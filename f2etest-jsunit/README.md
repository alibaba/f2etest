f2etest-jsunit
=======================

Jsunit client for f2etest.

Quick start
================

1. Install

    > npm install f2etest-jsunit -g

2. Run test

    > f2etest-jsunit --server http://f2etest.xxx.com/ --userid xxx --apikey xxx --browsers "chrome,ie 8" http://x.x.x.x/mocha.html

3. Run test loaded from json config

    > jsunit.json

        {
            "server": "http://f2etest.xxxx.com/",
            "userid": "xxxx",
            "apikey": "xxx",
            "browsers": "chrome",
            "url": "http://x.x.x.x/mocha.html",
            "hosts": "x.x.x.x www.alibaba.com",
            "coverageinclude": "/\\/src\\//i",
            "coverageexclude": "/\\/lib\\//i",
            "append": "\n  TEST CASE AMOUNT:{\"passed\":{!testPassed},\"failed\":{!testFailed},\"skipped\":{!testSkiped}}\n  CODE COVERAGE RESULT OF LINES IS: {!lineCovered}/{!lineCount}\n  CODE COVERAGE RESULT OF BRANCHES IS: {!branchCovered}/{!branchCount}"
        }

    > f2etest-jsunit

Append message
======================

Append parameter support vars:

* testPassed
* testFailed
* testSkiped
* testCount
* lineCovered
* lineCount
* branchCovered
* branchCount
* functionCovered
* functionCount

License
================

HTMLHint is released under the MIT license:

> The MIT License
>
> Copyright (c) 2015-2016 alibaba.com
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

* request: [https://github.com/request/request](https://github.com/request/request)
* colors: [https://github.com/Marak/colors.js](https://github.com/Marak/colors.js)
* commander: [https://github.com/tj/commander.js](https://github.com/tj/commander.js)
* xtend: [https://github.com/Raynos/xtend](https://github.com/Raynos/xtend)
