# class: Rational.
#
# primary authors: Trevor Baca, Victor Adan.
# optimization: Jared Grubb.
# mailto: trevorbaca (at) gmail (dot) com.
#
# svn version num: 89.

def gcd(a, b): 
    while b: 
        a, b = b, a % b 
    return abs(a) 

def lcm(*values):
    return reduce(lambda a, b: a * b / gcd(a, b), values, values[0])

class Rational(object):
    def __init__(self, n, d = 1):
        assert isinstance(n, int)
        assert isinstance(d, int)
        divisor = gcd(n, d)
        self.n = n / divisor
        self.d = d / divisor

    def __repr__(self):
        if self.d == 1:
            return str(self.n)
        else:
            return '%s/%s' % (self.n, self.d)

    def __neg__(self):
        return self.__class__(-self.n, self.d)

    def __invert__(self):
        return self.__class__(self.d, self.n)

    def __abs__(self):
        return self.__class__(abs(self.n), self.d)

    def __eq__(self, arg):
        return self.n == self.d * arg

    def __hash__(self):
        return self.__float__().__hash__()

    def __ne__(self, arg):
        return not self == arg

    def __gt__(self, arg):
         return self.n > self.d * arg

    def __ge__(self, arg):
         return self.n >= self.d * arg

    def __lt__(self, arg):
         return self.n < self.d * arg

    def __le__(self, arg):
         return self.n <= self.d * arg

    def __add__(self, arg):
        if not isinstance(arg, type(self)):
            arg = self.__class__(arg)
        n = self.n * arg.d + arg.n * self.d
        d = self.d * arg.d
        return self.__class__(n, d)

    def __radd__(self, arg):
        return self.__add__(arg)
      
    def __sub__(self, arg):
        return self.__add__(-arg)

    def __rsub__(self, arg):
        return self.__sub__(arg)

    def __mul__(self, arg):
        if not isinstance(arg, type(self)):
            arg = self.__class__(arg)
        n = self.n * arg.n
        d = self.d * arg.d
        return self.__class__(n, d)

    def __rmul__(self, arg):
        return self.__mul__(arg)
   
    def __div__(self, arg):
        if not isinstance(arg, type(self)):
            arg = self.__class__(arg)
        n = self.n * arg.d
        d = self.d * arg.n
        return self.__class__(n, d)

    def __rdiv__(self, arg):
        if not isinstance(arg, type(self)):
            arg = self.__class__(arg)
        return arg.__div__(self)

    def __truediv__(self, arg):
        if not isinstance(arg, type(self)):
            arg = self.__class__(arg)
        n = self.n * arg.d
        d = self.d * arg.n
        return self.__class__(n, d)

    def __rtruediv__(self, arg):
        if not isinstance(arg, type(self)):
            arg = self.__class__(arg)
        return arg.__truediv__(self)

    def __floordiv__(self, arg):
        from math import floor
        return self.__class__(int(floor(float(self / arg))))

    def __rfloordiv__(self, arg):
        from math import floor
        return self.__class__(int(floor(float(arg / self))))

    def __mod__(self, arg):
        return self - self // arg * arg

    def __rmod__(self, arg):
        if not isinstance(arg, type(self)):
            arg = self.__class__(arg)
        return arg % self

    def __int__(self):
        result = abs(self.n) // abs(self.d)
        if self >= 0:
            return result
        else:
            return -result

    def __float__(self):
        return float(self.n) / float(self.d)

    def set(self, n, d = 1):
        assert d > 0
        self.n = n
        self.d = d

    def copy(self):
        return self.__class__(self.n, self.d)

    @property
    def pair(self):
        return self.n, self.d
