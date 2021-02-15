# jDNS
Simple DNS stub resolver implemented in Java 15 by following [guide](https://github.com/EmilHernvall/dnsguide) from [Emil Hernvall](https://github.com/EmilHernvall).

### Working
- [x] A record
- [x] AAAA record
- [x] NS record
- [x] CNAME record
- [x] MX record
- [x] Single threaded UDP client

### Unimplemented
- [ ] Recursive resolver
- [ ] Multithreading
> ####  Low priority
- [ ] Other DNS records 
    - [ ] PTR
    - [ ] CERT
    - [ ] SRV
    - [ ] TXT
    - [ ] SOA
    - [ ] DNAME
    - [ ] IPSECKEY
    - [ ] DNSKEY
    - [ ] NSEC
    - [ ] RRSIG