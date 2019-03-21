const path = require('path');
const fs = require('fs');

const IGNORE_FILE_ARRAY = ['.DS_Store'];
/*
 * read proxy files -> generate proxy file
 */
class MergeProxy {
    merge(dirPath, desFilePath, type) {
        console.log(`ready to merge ${type}...`);

        const dir = fs.readdirSync(dirPath);
        let arr = {};
        dir.forEach(function (ele) {
            if (!IGNORE_FILE_ARRAY.includes(ele)) {
                const filePath = path.join(dirPath, ele);
                const data = fs.readFileSync(filePath, 'utf8');

                arr = {
                    ...arr,
                    [ele]: data,
                };
            }
        })

        const templatePath = path.join(__dirname, './template.js');
        const template = fs.readFileSync(templatePath, 'utf8');
        const file = template.replace(/\$\{(.+?)\}/g, (all, key) => {
            arr = JSON.stringify(arr);
            return arr;
        });

        fs.writeFileSync(desFilePath, file, 'utf8');

        console.log(`merge ${type} completed`);
    }

    build() {
        console.log('ready to build...');

        const docPath = path.join(__dirname, './doc');
        const newDocPath = path.join(__dirname, '../src/model/doc.js');
        const docListPath = path.join(__dirname, './toc');
        const newDocListPath = path.join(__dirname, '../src/model/toc.js');

        this.merge(docPath, newDocPath, 'doc');
        this.merge(docListPath, newDocListPath, 'doc list');

        return new Promise(resolve => {
            resolve('Data pull and storage completed');
        });
    }
}

module.exports = MergeProxy;
