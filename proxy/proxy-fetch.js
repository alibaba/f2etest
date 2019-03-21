const axios = require('axios');
const config = require('../src/config');
const wirteFile = require('./util/writeFile');

/*
 * fetchDocList -> recodeDocList -> fetchDoc -> record Doc
 */
class FetchProxy {
    async fetchDocList(namespace) {
        const { baseUrl } = config;
        const namespaceSplit = namespace.split('/');
        const user = namespaceSplit[0];
        const repo = namespaceSplit[1];

        // 获取一个仓库的目录结构
        const fetchUrl = `${baseUrl}/repos/${user}/${repo}/toc`;

        const options = {
            method: 'get',
            url: fetchUrl,
            headers: config.headers,
        }

        try {
            const res = await axios(options);
            const { data } = res.data;

            const result = await this.recordDocList(namespace, data);

            for (let i = 0; i < data.length; i++) {
                const doc = data[i];

                // 获取单个文档的详细信息
                const fetchDocUrl = `${baseUrl}/repos/${user}/${repo}/docs/${doc.slug}`;
                const result = await this.fetchDoc(fetchDocUrl, namespace);
                console.log(result);
            }

        } catch (e) {
            // console.error(e);
        }

        return new Promise(resolve => {
            resolve('Doc list pull and storage completed');
        });
    }

    async fetchDoc(url, namespace) {
        const options = {
            method: 'get',
            url,
            headers: config.headers,
        }

        try {
            const res = await axios(options);
            const { data } = res.data;

            const result = await this.recordDoc(data, namespace);

            return new Promise(resolve => {
                resolve(`Doc [${namespace}/${data.slug}] pull and storage completed`);
            });
        } catch (e) {
            return new Promise(resolve => {
                resolve(`Doc [${namespace}/${data.slug}] pull failed`);
            });
        }


    }

    recordDocList(namespace, data) {
        return new Promise(resolve => {
            const namespaceSplit = namespace.split('/');
            const user = namespaceSplit[0];
            const repo = namespaceSplit[1];
            const path = `${__dirname}/toc/${user}-${repo}`;

            wirteFile(path, data);

            resolve('done');
        });
    }

    recordDoc(data, namespace) {
        return new Promise(resolve => {
            const namespaceSplit = namespace.split('/');
            const user = namespaceSplit[0];
            const repo = namespaceSplit[1];

            const path = `${__dirname}/doc/${user}-${repo}-${data.slug}`;
            wirteFile(path, data);

            resolve('done');
        });
    }

    async fetch() {
        const { repos } = config;

        for (let i = 0; i < repos.length; i++) {
            const result = await this.fetchDocList(repos[i].namespace);
            console.log(result);
        }

        return new Promise(resolve => {
            resolve('Data pull and storage completed');
        });

    }
}

module.exports = FetchProxy;
