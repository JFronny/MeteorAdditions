"""Python Bindings to use masscan and access scan results."""
import logging
import os
import re
import shlex
import subprocess
import sys
from multiprocessing import Process
from xml.etree import ElementTree as ET

FORMAT = '[%(asctime)-15s] [%(levelname)s] [%(filename)s %(levelno)s line] %(message)s'
logger = logging.getLogger(__file__)
logging.basicConfig(format=FORMAT)
logger.setLevel(logging.DEBUG)


class NetworkConnectionError(Exception):
    pass


def __scan_progressive__(self, hosts, ports, arguments, callback, sudo):
    """Used by PortScannerAsync for callback."""
    try:
        scan_data = self._nm.scan(hosts, ports, arguments, sudo)
    except PortScannerError:
        scan_data = None

    if callback is not None:
        callback(hosts, scan_data)


class PortScanner(object):
    """Class which allows to use masscan from Python."""

    def __init__(self, masscan_search_path=(
            'masscan', '/usr/bin/masscan', '/usr/local/bin/masscan', '/sw/bin/masscan', '/opt/local/bin/masscan')):
        """
        Initialize the Port Scanner.
        * detects masscan on the system and masscan version
        * may raise PortScannerError exception if masscan is not found in the path
        :param masscan_search_path: tuple of string where to search for masscan executable. Change this if you want to use a specific version of masscan.
        :returns: nothing
        """
        self._masscan_path = ''  # masscan path
        self._scan_result = {}
        self._masscan_version_number = 0  # masscan version number
        self._masscan_subversion_number = 0  # masscan subversion number
        self._masscan_revised_number = 0  # masscan revised number
        self._masscan_last_output = ''  # last full ascii masscan output
        self._args = ''
        self._scaninfo = {}
        is_masscan_found = False  # true if we have found masscan

        self.__process = None

        # regex used to detect masscan (http or https)
        regex = re.compile(
            'Masscan version [0-9]*\.[0-9]*[^ ]* \( http(|s)://.* \)'
        )
        # launch 'masscan -V', we wait after
        # 'Masscan version 1.0.3 ( https://github.com/robertdavidgraham/masscan )'
        # This is for Mac OSX. When idle3 is launched from the finder, PATH is not set so masscan was not found
        for masscan_path in masscan_search_path:
            try:
                if sys.platform.startswith('freebsd') \
                        or sys.platform.startswith('linux') \
                        or sys.platform.startswith('darwin'):
                    p = subprocess.Popen(
                        [masscan_path, '-V'],
                        bufsize=10000,
                        stdout=subprocess.PIPE,
                        close_fds=True)
                else:
                    p = subprocess.Popen(
                        [masscan_path, '-V'],
                        bufsize=10000,
                        stdout=subprocess.PIPE)

            except OSError:
                pass
            else:
                self._masscan_path = masscan_path  # save path
                break
        else:
            raise PortScannerError(
                'masscan program was not found in path. PATH is : {0}'.format(os.getenv('PATH'))
            )
        self._masscan_last_output = p.communicate()[0]
        if isinstance(self._masscan_last_output, bytes):
            self._masscan_last_output = self._masscan_last_output.decode('utf-8')
        for line in self._masscan_last_output.split(os.linesep):
            if regex.match(line):
                is_masscan_found = True
                # Search for version number
                regex_version = re.compile(r'(?P<version>\d{1,4})\.(?P<subversion>\d{1,4})\.(?P<revised>\d{1,4})')
                rv = regex_version.search(line)

                if rv:
                    # extract version/subversion/revised
                    self._masscan_version_number = int(rv.group('version'))
                    self._masscan_subversion_number = int(rv.group('subversion'))
                    self._masscan_revised_number = int(rv.group('revised'))
                break

        if not is_masscan_found:
            raise PortScannerError('masscan program was not found in path')

    def __getitem__(self, host):
        """Return a host detail."""
        assert type(host) is str, 'Wrong type for [host], should be a string [was {0}]'.format(type(host))
        return self._scan_result['scan'][host]

    @property
    def get_masscan_last_output(self):
        """
        Return the last text output of masscan in raw text
        this may be used for debugging purpose.
        :returns: string containing the last text output of masscan in raw text
        """
        return self._masscan_last_output

    @property
    def masscan_version(self):
        """
        Return the masscan version if detected (int version, int subversion)
        or (0, 0) if unknown.
        :returns: masscan_version_number, masscan_subversion_number
        """
        return self._masscan_version_number, self._masscan_subversion_number, self._masscan_revised_number

    @property
    def all_hosts(self):
        """Return a sorted list of all hosts."""
        if not 'scan' in list(self._scan_result.keys()):
            return []
        listh = list(self._scan_result['scan'].keys())
        listh.sort()
        return listh

    @property
    def command_line(self):
        """
        Return command line used for the scan.
        may raise AssertionError exception if called before scanning
        """
        assert 'masscan' in self._scan_result, 'Do a scan before trying to get result !'
        assert 'command_line' in self._scan_result['masscan'], 'Do a scan before trying to get result !'

        return self._scan_result['masscan']['command_line']

    @property
    def scan_result(self):
        """
        Return command line used for the scan.
        may raise AssertionError exception if called before scanning
        """
        assert 'masscan' in self._scan_result, 'Do a scan before trying to get result !'

        return self._scan_result

    @property
    def scaninfo(self):
        """
        Return scaninfo structure.
        {'tcp': {'services': '22', 'method': 'connect'}}
        may raise AssertionError exception if called before scanning
        """
        return self._scaninfo

    @property
    def scanstats(self):
        """
        Return the scanstats structure.
        {'uphosts': '3', 'timestr': 'Thu Jun  3 21:45:07 2010', 'downhosts': '253', 'totalhosts': '256', 'elapsed': '5.79'}
        may raise AssertionError exception if called before scanning
        """
        assert 'masscan' in self._scan_result, 'Do a scan before trying to get result !'
        assert 'scanstats' in self._scan_result['masscan'], 'Do a scan before trying to get result !'

        return self._scan_result['masscan']['scanstats']

    def scan(self, hosts, ports, arguments, sudo=False):
        """
        Scan given hosts.
        May raise PortScannerError exception if masscan output was not XML
        Test existence of the following key to know
        if something went wrong : ['masscan']['scaninfo']['error']
        If not present, everything was ok.
        :param hosts: string for hosts as masscan use it 'scanme.masscan.org' or '198.116.0-255.1-127' or '216.163.128.20/20'
        :param ports: string for ports as masscan use it '22,53,110,143-4564'
        :param arguments: string of arguments for masscan '-sU -sX -sC'
        :param sudo: launch masscan with sudo if True
        :returns: scan_result as dictionary
        """
        assert type(hosts) is str, 'Wrong type for [hosts], should be a string [was {0}]'.format(
            type(hosts))  # noqa
        assert type(ports) in (str, type(None)), 'Wrong type for [ports], should be a string [was {0}]'.format(
            type(ports))  # noqa
        assert type(arguments) is str, 'Wrong type for [arguments], should be a string [was {0}]'.format(
            type(arguments))  # noqa

        h_args = shlex.split(hosts)
        f_args = shlex.split(arguments)

        # Launch scan
        args = [self._masscan_path, '-oX', '-'] + h_args + ['-p', ports] * (ports is not None) + f_args

        logger.debug('Scan parameters: "' + ' '.join(args) + '"')
        self._args = ' '.join(args)

        if sudo:
            args = ['sudo'] + args

        p = subprocess.Popen(
            args,
            bufsize=100000,
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE
        )

        # wait until finished
        # get output
        self._masscan_last_output, masscan_err = p.communicate()
        if isinstance(self._masscan_last_output, bytes):
            self._masscan_last_output = self._masscan_last_output.decode('utf-8')
        if isinstance(masscan_err, bytes):
            masscan_err = masscan_err.decode('utf-8')

        # If there was something on stderr, there was a problem so abort...  in
        # fact not always. As stated by AlenLPeacock :
        # This actually makes python-masscan mostly unusable on most real-life
        # networks -- a particular subnet might have dozens of scannable hosts,
        # but if a single one is unreachable or unroutable during the scan,
        # masscan.scan() returns nothing. This behavior also diverges significantly
        # from commandline masscan, which simply stderrs individual problems but
        # keeps on trucking.

        masscan_err_keep_trace = []
        masscan_warn_keep_trace = []
        if len(masscan_err) > 0:
            regex_warning = re.compile('^Warning: .*', re.IGNORECASE)
            for line in masscan_err.split(os.linesep):
                if len(line) > 0:
                    rgw = regex_warning.search(line)
                    if rgw is not None:
                        # sys.stderr.write(line+os.linesep)
                        masscan_warn_keep_trace.append(line + os.linesep)
                    else:
                        # raise PortScannerError(masscan_err)
                        masscan_err_keep_trace.append(masscan_err)

        return self.analyse_masscan_xml_scan(
            masscan_xml_output=self._masscan_last_output,
            masscan_err=masscan_err,
            masscan_err_keep_trace=masscan_err_keep_trace,
            masscan_warn_keep_trace=masscan_warn_keep_trace
        )

    def analyse_masscan_xml_scan(self, masscan_xml_output=None, masscan_err='', masscan_err_keep_trace='',
                                 masscan_warn_keep_trace=''):
        """
        Analyse the NMAP XML scan ouput.
        May raise PortScannerError exception if masscan output was not xml
        Test existance of the following key to know if something went wrong : ['masscan']['scaninfo']['error']
        If not present, everything was ok.
        :param masscan_xml_output: XML string to analyse
        :returns: scan_result as dictionnary
        """

        # masscan xml output looks like :
        """
        <?xml version="1.0"?>
        <!-- masscan v1.0 scan -->
        <?xml-stylesheet href="" type="text/xsl"?>
        <nmaprun scanner="masscan" start="1490242774" version="1.0-BETA"  xmloutputversion="1.03">
            <scaninfo type="syn" protocol="tcp" />
            <host endtime="1490242774">
                <address addr="10.0.9.9" addrtype="ipv4"/>
                <ports>
                    <port protocol="tcp" portid="80">
                        <state state="open" reason="syn-ack" reason_ttl="64"/>
                    </port>
                </ports>
                </host>
            <host endtime="1490242774"><address addr="10.0.9.254" addrtype="ipv4"/><ports><port protocol="tcp" portid="80"><state state="open" reason="syn-ack" reason_ttl="255"/></port></ports></host>
            <host endtime="1490242774"><address addr="10.0.9.19" addrtype="ipv4"/><ports><port protocol="tcp" portid="80"><state state="open" reason="syn-ack" reason_ttl="64"/></port></ports></host>
            <host endtime="1490242774"><address addr="10.0.9.49" addrtype="ipv4"/><ports><port protocol="tcp" portid="80"><state state="open" reason="syn-ack" reason_ttl="64"/></port></ports></host>
            <host endtime="1490242774"><address addr="10.0.9.8" addrtype="ipv4"/><ports><port protocol="tcp" portid="80"><state state="open" reason="syn-ack" reason_ttl="64"/></port></ports></host>
            <host endtime="1490242775"><address addr="10.0.9.11" addrtype="ipv4"/><ports><port protocol="tcp" portid="80"><state state="open" reason="syn-ack" reason_ttl="64"/></port></ports></host>
            <host endtime="1490242775"><address addr="10.0.9.10" addrtype="ipv4"/><ports><port protocol="tcp" portid="80"><state state="open" reason="syn-ack" reason_ttl="64"/></port></ports></host>
            <host endtime="1490242775"><address addr="10.0.9.6" addrtype="ipv4"/><ports><port protocol="tcp" portid="80"><state state="open" reason="syn-ack" reason_ttl="64"/></port></ports></host>
            <host endtime="1490242775"><address addr="10.0.9.12" addrtype="ipv4"/><ports><port protocol="tcp" portid="80"><state state="open" reason="syn-ack" reason_ttl="64"/></port></ports></host>
            <host endtime="1490242776"><address addr="10.0.9.28" addrtype="ipv4"/><ports><port protocol="tcp" portid="80"><state state="open" reason="syn-ack" reason_ttl="64"/></port></ports></host>
            <host endtime="1498802982"><address addr="10.0.9.29" addrtype="ipv4"/><ports><port protocol="tcp" portid="80"><state state="open" reason="response" reason_ttl="48"/><service name="title" banner="401 - Unauthorized"></service></port></ports></host>
            <runstats>
                <finished time="1490242786" timestr="2017-03-23 12:19:46" elapsed="13" />
                <hosts up="10" down="0" total="10" />
            </runstats>
        </nmaprun>
        """

        if masscan_xml_output is not None:
            self._masscan_last_output = masscan_xml_output

        scan_result = {}

        try:
            dom = ET.fromstring(self._masscan_last_output)
        except Exception:
            if "found=0" in masscan_err:
                raise NetworkConnectionError("network is unreachable.")
            if len(masscan_err_keep_trace) > 0:
                raise PortScannerError(masscan_err)
            else:
                raise PortScannerError(self._masscan_last_output)

        # masscan command line
        scan_result['masscan'] = {
            'command_line': self._args,
            'scanstats': {
                'timestr': dom.find("runstats/finished").get('timestr'),
                'elapsed': dom.find("runstats/finished").get('elapsed'),
                'uphosts': dom.find("runstats/hosts").get('up'),
                'downhosts': dom.find("runstats/hosts").get('down'),
                'totalhosts': dom.find("runstats/hosts").get('total')}
        }

        # if there was an error
        if len(masscan_err_keep_trace) > 0:
            self._scaninfo['error'] = masscan_err_keep_trace

        # if there was a warning
        if len(masscan_warn_keep_trace) > 0:
            self._scaninfo['warning'] = masscan_warn_keep_trace

        scan_result['scan'] = {}

        for dhost in dom.findall('host'):
            # host ip, mac and other addresses
            host = None
            address_block = {}
            vendor_block = {}
            endtime = dhost.get('endtime')
            for address in dhost.findall('address'):
                addtype = address.get('addrtype')
                address_block[addtype] = address.get('addr')
                if addtype == 'ipv4':
                    host = address_block[addtype]
                elif addtype == 'mac' and address.get('vendor') is not None:
                    vendor_block[address_block[addtype]] = address.get('vendor')

            if host is None:
                host = dhost.find('address').get('addr')
            if host not in scan_result['scan']:
                scan_result['scan'][host] = {}

            for dport in dhost.findall('ports/port'):
                proto = dport.get('protocol')
                port = int(dport.get('portid'))
                state = dport.find('state').get('state')
                reason = dport.find('state').get('reason')
                reason_ttl = dport.find('state').get('reason_ttl')
                services = []

                if not proto in list(scan_result['scan'][host].keys()):
                    scan_result['scan'][host][proto] = {}

                scan_result['scan'][host][proto][port] = {
                    'state': state,
                    'reason': reason,
                    'reason_ttl': reason_ttl,
                    'endtime': endtime,
                }

                for service in dhost.findall('ports/port/service'):
                    services.append({
                        'name': service.get('name'),
                        'banner': service.get('banner'),
                    })
                scan_result['scan'][host][proto][port]['services'] = services

        self._scan_result = scan_result
        return scan_result

    def has_host(self, host):
        """If host has result it returns True, False otherwise."""
        assert type(host) is str, 'Wrong type for [host], should be a string [was {0}]'.format(type(host))
        assert 'scan' in self._scan_result, 'Do a scan before trying to get result !'

        if host in list(self._scan_result['scan'].keys()):
            return True

        return False


class PortScannerAsync(object):
    """
    PortScannerAsync allows to use masscan from python asynchronously
    for each host scanned, callback is called with scan result for the host.
    """

    def __init__(self):
        """
        Initialize the module.
        * detects masscan on the system and masscan version
        * may raise PortScannerError exception if masscan is not found in the path
        """
        self._process = None
        self._nm = PortScanner()

    def __del__(self):
        """
        Cleanup when deleted
        """
        if self._process is not None:
            try:
                if self._process.is_alive():
                    self._process.terminate()
            except AssertionError:
                # Happens on python3.4
                # when using PortScannerAsync twice in a row
                pass
        self._process = None

    def scan(self, hosts, ports, arguments, callback=None, sudo=False):
        """
        Scan given hosts in a separate process and return host by host result using callback function
        PortScannerError exception from standard masscan is catched and you won't know about but get None as scan_data
        :param hosts: string for hosts as masscan use it 'scanme.masscan.org' or '198.116.0-255.1-127' or '216.163.128.20/20'
        :param ports: string for ports as masscan use it '22,53,110,143-4564'
        :param arguments: string of arguments for masscan '-sU -sX -sC'
        :param callback: callback function which takes (host, scan_data) as arguments
        :param sudo: launch masscan with sudo if true
        """
        assert type(hosts) is str, 'Wrong type for [hosts], should be a string [was {0}]'.format(type(hosts))
        assert type(ports) in (str, type(None)), 'Wrong type for [ports], should be a string [was {0}]'.format(
            type(ports))
        assert type(arguments) is str, 'Wrong type for [arguments], should be a string [was {0}]'.format(
            type(arguments))

        assert callable(callback) or callback is None, 'The [callback] {0} should be callable or None.'.format(
            str(callback))

        self._process = Process(
            target=__scan_progressive__,
            args=(self, hosts, ports, arguments, callback, sudo)
        )
        self._process.daemon = True
        self._process.start()

    def stop(self):
        """Stop the current scan process.
        """
        if self._process is not None:
            self._process.terminate()

    def wait(self, timeout=None):
        """
        Wait for the current scan process to finish, or timeout
        :param timeout: default = None, wait timeout seconds
        """
        assert type(timeout) in (
            int, type(None)), 'Wrong type for [timeout], should be an int or None [was {0}]'.format(type(timeout))

        self._process.join(timeout)

    def still_scanning(self):
        """
        :returns: True if a scan is currently running, False otherwise
        """
        try:
            return self._process.is_alive()
        except:
            return False


class PortScannerYield(PortScannerAsync):
    """
    PortScannerYield allows to use masscan from python with a generator
    for each host scanned, yield is called with scan result for the host
    """

    def __init__(self):
        """
        Initialize the module
        * detects masscan on the system and masscan version
        * may raise PortScannerError exception if masscan is not found in the path
        """
        PortScannerAsync.__init__(self)

    def scan(self, hosts, ports, arguments, sudo=False):
        """
        Scan given hosts in a separate process and return host by host result using callback function
        PortScannerError exception from standard masscan is catched and you won't know about it
        :param hosts: string for hosts as masscan use it 'scanme.masscan.org' or '198.116.0-255.1-127' or '216.163.128.20/20'
        :param ports: string for ports as masscan use it '22,53,110,143-4564'
        :param arguments: string of arguments for masscan '-sU -sX -sC'
        :param sudo: launch masscan with sudo if true
        """

        assert type(hosts) is str, 'Wrong type for [hosts], should be a string [was {0}]'.format(type(hosts))
        assert type(ports) in (str, type(None)), 'Wrong type for [ports], should be a string [was {0}]'.format(
            type(ports))
        assert type(arguments) is str, 'Wrong type for [arguments], should be a string [was {0}]'.format(
            type(arguments))

        for host in self._nm.listscan(hosts):
            try:
                scan_data = self._nm.scan(host, ports, arguments, sudo)
            except PortScannerError:
                scan_data = None
            yield (host, scan_data)
        return

    def stop(self):
        pass

    def wait(self, timeout=None):
        pass

    def still_scanning(self):
        pass


class PortScannerError(Exception):
    """Exception error class for PortScanner class."""

    def __init__(self, value):
        """Initialize the exception."""
        self.value = value

    def __str__(self):
        """String representation of a value."""
        return repr(self.value)

    def __repr__(self):
        """Representation of an exception."""
        return 'PortScannerError exception {0}'.format(self.value)