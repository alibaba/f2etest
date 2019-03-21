// 将前序遍历结果转化为有父子节点关系的数据结构
function preorderToTree(tree) {
    let currentDepth = 1;

    // 获取最大深度
    tree.forEach((node) => {
        if (node.depth > currentDepth) {
            currentDepth = node.depth;
        }
    });

    while (currentDepth > 1) {
        searchChildNodes(tree, currentDepth);
        currentDepth--;
    }

    return tree;
}

// 遍历指定深度的树节点
function searchChildNodes(tree, currentDepth) {
    for (let index = 0; index < tree.length; index++) {
        insertChildNodes(tree, index, currentDepth);
    }
}

// 将指定深度的子节点插入其父节点
function insertChildNodes(tree, index, currentDepth) {
    if (index < tree.length && tree[index].depth === currentDepth) {
        if (!Array.isArray(tree[index - 1].childNodes)) {
            tree[index - 1].childNodes = []
        }

        tree[index - 1].childNodes.push(tree[index]);
        tree.splice(index, 1);

        // 若该子节点其后为同深度的节点，则继续插入父节点
        insertChildNodes(tree, index, currentDepth);
    }
}

export default preorderToTree;