# Design a URL Shortener system like Bit.ly that can convert long URLs into unique short aliases and 
# retrieve the original URLs efficiently. Implement a self-contained solution 
# without using any external database, suitable for demo purposes.

import string

class URLShortener:
    def __init__(self):
        self.url_db = {}
        self.reverse_db = {}
        self.counter = 1
        self.alphabet = string.ascii_letters + string.digits

    def encode(self, num):
        if num == 0:
            return self.alphabet[0]
        s = []
        base = len(self.alphabet)
        while num > 0:
            s.append(self.alphabet[num % base])
            num //= base
        return ''.join(reversed(s))
    
    def shorten(self, long_url):
        if long_url in self.reverse_db:
            return self.reverse_db[long_url]
        short_url = self.encode(self.counter)
        self.url_db[short_url] = long_url
        self.reverse_db[long_url] = short_url
        self.counter += 1
        return short_url
    
    def retrieve(self, short_url):
        return self.url_db.get(short_url, None)
    
# -------------------------------
# Demo
# -------------------------------
shortener = URLShortener()

url1 = "https://www.youtube.com/@code-with-Bharadwaj"
url2 = "https://github.com/Manu577228"

s1 = shortener.shorten(url1)
s2 = shortener.shorten(url2)

print("Short URL for YouTube:", s1)
print("Short URL for GitHub :", s2)
print("Retrieve YouTube URL  :", shortener.retrieve(s1))
print("Retrieve GitHub URL   :", shortener.retrieve(s2))