const config = {
    baseUrl: 'https://www.yuque.com/api/v2',
    headers: {
        'User-Agent': 'no-doc',
            'X-Auth-Token': 'WdhcLYONf8AvaNFnQPkw0yGdZxXsbXKOTpQZdsO9',
    },
    title: 'F2etest',
    repos: [
        {
            name: 'F2etest',
            namespace: 'https://www.yuque.com/linshuoting/f2etest',
        },
    ],
    githubUrl: 'https://github.com/alibaba/f2etest',
};

config.repos.forEach((repo) => {
    // format namespace
    repo.namespace = repo.namespace.replace(/\https:\/\/www.yuque.com\//, '');
})

module.exports = config;