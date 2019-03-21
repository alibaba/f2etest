const fs = require('fs');

function wirteFile(path, data) {
    const formatData = JSON.stringify(data);

    fs.exists(path, (isExisted) => {
        if (!isExisted) {
            const file = fs.openSync(path, 'w');
            fs.closeSync(file);
        }

        fs.writeFileSync(path, formatData, 'utf8');
    })

}

module.exports = wirteFile;
