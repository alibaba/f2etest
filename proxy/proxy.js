const FetchProxy = require('./proxy-fetch');
const MergeProxy = require('./proxy-merge');

/*
 * run: fetch proxy -> merge proxy
 */
class Proxy {
    async run() {
        const fetchProxy = new FetchProxy();
        const mergeProxy = new MergeProxy();

        const fetchResult = await fetchProxy.fetch();

        console.log(fetchResult);
        console.log('=========================================');

        const mergeResult = await mergeProxy.build();
        console.log(mergeResult);
    }
}

const proxy = new Proxy();
proxy.run();