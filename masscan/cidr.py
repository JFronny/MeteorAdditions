import copy
import re


cidr_pattern = re.compile("(\\d{1,3}.){3}\\d{1,3}/\\d{1,2}")


def __ip_to_int(ipaddr):  # return int
    """
    Convert an ipv4 "dotted-quad" string to an integer.
    """
    a, b, c, d = ipaddr.split('.')
    i = int(a) * (2 ** 24)
    i += int(b) * (2 ** 16)
    i += int(c) * (2 ** 8)
    i += int(d)
    return i


def __int_to_ip(i):  # return string
    """
    Convert an integer to an ipv4 "dotted-quad" string.
    """
    a = i // 2 ** 24
    b = (i - a * 2 ** 24) // 2 ** 16
    c = (i - a * 2 ** 24 - b * 2 ** 16) // 2 ** 8
    d = (i - a * 2 ** 24 - b * 2 ** 16 - c * 2 ** 8)
    return '%d.%d.%d.%d' % (a, b, c, d)


def __cidr_to_range(cidr):  # return tuple of 2 ints
    """
    Find starting and ending IPs of the provided CIDR
    and return those two addresses as a tuple of integers.
    """
    if '/' in cidr:
        ip, maskbits_s = cidr.split('/', 1)
        mbits = int(maskbits_s)
        if mbits < 1 or mbits > 32:
            raise ValueError(
                '[%s] Invalid network mask size: %d' % (cidr, mbits))
        start = __ip_to_int(ip)
        end = start + 2 ** (32 - mbits) - 1
        if start % (2 ** (32 - mbits)) != 0:
            x = start - (start % (2 ** (32 - mbits)))
            suggested_start0 = __int_to_ip(x)
            suggested_start1 = __int_to_ip(x + (1 << (32 - mbits)))
            raise ValueError('[%s] Invalid starting address for /%d. Try %s or %s' % (
                cidr, mbits, suggested_start0, suggested_start1))
    else:
        start = __ip_to_int(cidr)
        end = start
    return start, end


def __range_to_cidrs(range_list):  # return list of strings
    """
    From a list of IP ranges (each provided as a tuple of integers),
    produce a list of CIDR strings that together cover all ranges
    without duplication or overlap.
    """
    outlist = []
    a = range_list[0]
    b = range_list[1]
    span = b - a + 1
    if span == 1:
        outlist.append('%s/32' % __int_to_ip(a))
        return outlist
    i = 0
    while i < 33:
        x = span >> i
        if x == 1:
            while True:
                if a % (2 ** i) == 0:
                    break
                elif i == -1:
                    raise ValueError('Failed to construct a valid CIDR!')
                else:
                    i -= 1
            mbits = 32 - i
            outlist.append('%s/%d' % (__int_to_ip(a), mbits))
            consumed = (1 << i)
            remainder = span - consumed
            if remainder > 0:
                a += consumed
                span = remainder
                i = 0
            else:
                break
        elif x == 0:
            raise ValueError("Failed to identify suitable mask size for span=%s" % span)
        else:
            i += 1
    return outlist


def __compare_ranges(a, b):  # return None or tuple
    """
    Compare two ranges. If the two can be replaced with one,
    return that one range (tuple), otherwise return None.
    """
    # identical
    if a[0] == b[0] and a[1] == b[1]:
        return None
    # a completely inside b
    if a[0] >= b[0] and a[1] <= b[1]:
        return b
    # b completely inside a
    if b[0] >= a[0] and b[1] <= a[1]:
        return None
    # aaaa
    #   bbbb  <- a/b overlap or immediately adjacent
    if a[0] < b[0] <= a[1] + 1 and a[1] < b[1]:
        return a[0], b[1]
    #   aaaa
    # bbbb    <- b/a overlap or immediately adjacent
    if b[0] < a[0] <= b[1] + 1 and b[1] < a[1]:
        return b[0], a[1]
    # no change
    return None


def flatten(_2d_list):
    flat_list = []
    for element in _2d_list:
        if type(element) is list:
            for item in flatten(element):
                flat_list.append(item)
        else:
            flat_list.append(element)
    return flat_list


def merge(cidr_ranges):
    ranges = list(map(__cidr_to_range, cidr_ranges))
    out_ranges = []
    iterations = 0
    while True:
        iterations += 1
        ranges.sort()
        for A in ranges:
            for B in ranges:
                result = __compare_ranges(A, B)
                if result:
                    A = copy.deepcopy(result)
            if A not in out_ranges:
                out_ranges.append(A)
        if sorted(ranges) == sorted(out_ranges):
            break
        else:
            ranges = copy.deepcopy(out_ranges)
            out_ranges = []

    return flatten(list(map(__range_to_cidrs, sorted(out_ranges))))


def is_ipv4(cidr):
    return cidr_pattern.fullmatch(cidr)
