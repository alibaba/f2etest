f2etest-local
=======================

It is a local proxy for f2etest.

Use in cli
======================

Install:

    npm install f2etest-local -g

Star local proxy:

    f2etest-local -V
    f2etest-local --help
    f2etest-local start --port 1080
    f2etest-local start --port 1080 --server http://f2etest.xxx.com/ --name test1 --apikey keystring
    f2etest-local stop

Tip: If the server parameter is not set, just a simple local proxy.

Use in nodejs
=====================

Install:

    npm install f2etest-local --save

Simple use:

    var f2etestLocal = require('f2etest-local');

    f2etestLocal.start({
        server: 'http://f2etest.xxx.com/',
        name: 'username',
        apikey: 'apikey',
        port: 1080
    });

Hosts mode::

    var f2etestLocal = require('f2etest-local');

    f2etestLocal.start({
        server: 'http://f2etest.xxx.com/',
        name: 'username',
        apikey: 'apikey',
        port: 1080,
        mode: 'hosts',
        hosts: '127.0.0.1 www.alibaba.com\r\n127.0.0.2 www.google.com'
    });

Forward mode(forder to anther proxy):

    var f2etestLocal = require('f2etest-local');

    f2etestLocal.start({
        server: 'http://f2etest.xxx.com/',
        name: 'username',
        apikey: 'apikey',
        port: 1080,
        mode: 'forward',
        forwardHost: '192.168.0.1',
        forwardPort: 8080
    });

License
================

HTMLHint is released under the MIT license:

> The MIT License
>
> Copyright (c) 2015 alibaba.com
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

* commander.js: [https://github.com/visionmedia/commander.js](https://github.com/visionmedia/commander.js)
* colors.js: [https://github.com/Marak/colors.js](https://github.com/Marak/colors.js)
* request: [https://github.com/request/request](https://github.com/request/request)
