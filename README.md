# jDNS

Simple DNS stub resolver implemented in Java 15 by following [guide](https://github.com/EmilHernvall/dnsguide)
from [Emil Hernvall](https://github.com/EmilHernvall).

### Working

- [x] A record
- [x] AAAA record
- [x] NS record
- [x] CNAME record
- [x] MX record
- [x] Single threaded UDP server forwarding queries to Google's DNS server (8.8.8.8)

### Unimplemented

- [ ] Recursive resolver
- [ ] Multithreading

> #### Low priority

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

## Usage

After running [DNSReslover](src/main/java/DnsResolver.java) server will be listening for queries on address `127.0.0.1`
and port `5053`.

Query **jDNS** resolver by using DNS lookup tools such as [dig](https://www.isc.org/download/). For example run this
command to lookup A record for [google.com](https://google.com):

```bash
dig @127.0.0.1 -p 5053 google.com
```

Expected response:

```bash
; <<>> DiG 9.16.11 <<>> @127.0.0.1 -p 5053 google.com
; (1 server found)
;; global options: +cmd
;; Got answer:
;; ->>HEADER<<- opcode: QUERY, status: NOERROR, id: 19711
;; flags: qr rd ra; QUERY: 1, ANSWER: 1, AUTHORITY: 0, ADDITIONAL: 0

;; QUESTION SECTION:
;google.com.                    IN      A

;; ANSWER SECTION:
google.com.             299     IN      A       172.217.20.14

;; Query time: 109 msec
;; SERVER: 127.0.0.1#5053(127.0.0.1)
;; WHEN: Tue Feb 16 18:58:33 Central European Standard Time 2021
;; MSG SIZE  rcvd: 54
```

Expected output to server terminal:

```
>>> RECEIVED QUERY <<<
DnsQuestion {
name = 'google.com'
queryType = A
}
>>> ANSWER <<<
A {
domain = 'google.com'
addr = 172.217.20.14
ttl = 299
}
```