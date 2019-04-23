import React from "react";
import { Link } from 'react-router-dom';
import { Menu } from 'antd';

import Toc from '../model/toc';
import preorderToTree from "./util/preorderToTree";

const { SubMenu } = Menu;

class App extends React.Component {
    constructor(props) {
        super(props);
        const { match } = props;
        this.state = {
            match
        };
    }

    componentDidMount() {
        const { match } = this.props;
        this.setState({
            match,
        });
    }

    componentWillReceiveProps(nextProps) {
        this.props = nextProps;
        const { match } = this.props;
        this.setState({
            match,
        });
    }

    renderNodes(node) {
        const { match } = this.state;
        const { user, repo } = match.params;
        const { childNodes = [] } = node;

        const link = node.slug === '#' ?
            node.title :
            <Link className="default-link-text" to={`/${user}/${repo}/${node.slug}`}>{node.title}</Link>;

        if (!childNodes.length) {
            return (
                <Menu.Item>
                    {link}
                </Menu.Item>
            );
        } else {
            return (
                <SubMenu title={link} {...this.props}>
                    {
                        childNodes.map((node) => this.renderNodes(node))
                    }
                </SubMenu>
            )
        }
    }

    render() {
        const { match } = this.state;
        const { user, repo } = match.params;
        let TocList = [];

        if (Object.keys(user).length && Object.keys(repo).length) {
            const tocNamespace = `${user}-${repo}`;

            TocList = Toc[tocNamespace];
            TocList = JSON.parse(TocList);
        }

        const tree = preorderToTree(TocList);

        return (
            <Menu
                className="flex-sidebar"
                style={{ minHeight: `${window.screen.availHeight}px` }}
                mode="inline"
            >
                {
                    tree.map((node, index) => this.renderNodes(node, index))
                }
            </Menu>
        );
    }
}

export default App;