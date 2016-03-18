f2etest-webdriver
=======================

WebDriver client for f2etest.

Quick start
================

1. Install

    > npm install f2etest-webdriver

2. Run test

    > node test.js

        var F2etestWebDriver = require('f2etest-webdriver');

        F2etestWebDriver.run({
            server: 'http://f2etest.xxx.com/',
            userid: 'xxxxx',
            apiKey: '1122334455667788',
            browserName: 'chrome',
            browserVersion: '',
            hosts: '',
            proxy: ''
        }, function*(error, browser){
            if(browser){
                yield browser.url('https://www.baidu.com/');
                console.log(yield browser.title());
                yield browser.close();
            }
            else{
                console.log(error);
            }
        });

Mocha demo
================

1. Install dependencies

    > npm install mocha -g

    > npm install expect.js mocha-generators f2etest-webdriver

2. Run test code

    > mocha test.js

        var F2etestWebDriver = require('f2etest-webdriver');
        var expect  = require("expect.js");
        require('mocha-generators').install();

        describe('f2etest-webdriver test', function(){

            this.timeout(10000);

            var browser;
            before(function*(){
                browser = yield F2etestWebDriver.run({
                    server: 'http://f2etest.xxx.com/',
                    userid: 'xxxxx',
                    apiKey: '1122334455667788',
                    browserName: 'chrome',
                    browserVersion: '',
                    hosts: '',
                    proxy: ''
                });
            });

            it('should open url', function*(){
                yield browser.url('https://www.baidu.com/');
                var kw = yield browser.find('#kw');
                expect(kw.length).to.be(1);

                yield kw.val('mp3').submit();

                var url = yield browser.url();
                expect(url).to.contain('wd=mp3');
            });

            after(function*(){
                yield browser.close();
            });

        });

Api list
================

F2etestWebDriver.run method
----------------

    F2etestWebDriver.run({
        server: 'http://f2etest.xxx.com/',
        userid: 'xxxx',
        apiKey: '1122334455667788',
        browserName: 'chrome',
        browserVersion: '',
        hosts: '',
        proxy: ''
    });

1. server: f2etest server url
2. userid: f2etest userid
3. apiKey: f2etest apiKey
4. browserName: browser name supported by f2etest
5. browserVersion: browser version supported by f2etest
6. hosts: hosts for test
7. proxy: proxy forward for test
8. logLevel: 0: no log, 1: warning & error, 2: all log
9. nocolor: true(no color)
10. speed: 0(default: 0 ms), used for delay test

License
================

HTMLHint is released under the MIT license:

> The MIT License
>
> Copyright (c) 2016 alibaba.com
>
> Permission is hereby granted, free of charge, to any person obtaining a copy
> of this software and associated documentation files (the "Software"), to deal
> in the Software without restriction, including without limitation the rights
> to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
> copies of the Software, and to permit persons to whom the Software is
> furnished to do so, subject to the following conditions:
>
> The above copyright notice and this permission notice shall be included in
> all copies or substantial portions of the Software.
>
> THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
> IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
> FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
> AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
> LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
> OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
> THE SOFTWARE.

Thanks
================

* co: [https://github.com/tj/co](https://github.com/tj/co)
* jWebDriver: [https://github.com/yaniswang/jWebDriver](https://github.com/yaniswang/jWebDriver)
* request: [https://github.com/request/request](https://github.com/request/request)
