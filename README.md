Remember to modify the `config.js` file, for example:

```
{
    baseUrl: 'https://www.yuque.com/api/v2',        // yuque api base url
    headers: {
        'User-Agent': 'no-doc',
        'X-Auth-Token': 'WdhcLYONf8AvaNFnQPkw0yGdZxXsbXKOTpQZdsO9',
    },                                              // http headers
    title: 'Noform',                                // navigation bar title
    repos: [
        {
            name: 'NoForm',                         // the tile of repository
            namespace: 'https://www.yuque.com/guishu/noform/brief-intro', // the default document url
        },
        {
            name: '开发者文档',
            namespace: 'https://www.yuque.com/yuque/developer/api',
        },
    ],
    githubUrl: 'https://github.com/alibaba/noform', // your github repository url
}
```

You can start the project by following the steps below:
```
npm i
npm run proxy
npm start
```

Or build your resources by following the steps below:
```
npm i
npm run proxy
npm build
```

Reference doc:
> [yuque developer api](https://www.yuque.com/yuque/developer/api)