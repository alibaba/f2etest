import React from "react";
import { Menu, Row, Col } from 'antd';
import { Link } from 'react-router-dom';

import logo from '../svg/logo.svg';
import github from '../svg/github-icon.svg';
import config from "../config.js";

class App extends React.Component {
    constructor(props) {
        super(props);
        const { match } = props;
        const { params } = match || {};
        const { user, repo } = params || {};
        const url = `${user}/${repo}`;
        const current = config.repos.findIndex(item => item.namespace.indexOf(url) !== -1);
        this.state = {
            current: '' + (current < 0 ? 0 : current)
        };
    }

    handleClick = (e) => {
        this.setState({
            current: e.key,
        });
    }

    render() {
        return (
            <Row>
                <Col span={6}>
                    <div style={{ lineHeight: '64px', paddingLeft: '20px', borderBottom: '1px solid #e8e8e8' }}>
                        <img src={logo} className="app-logo" alt="logo" />
                        <span className="font-weight">{config.title}</span>
                    </div>
                </Col>
                <Col span={16}>
                    <Menu
                        onClick={this.handleClick}
                        selectedKeys={[this.state.current]}
                        mode="horizontal"
                    >
                        {config.repos.map((repo, index) => {
                            return <Menu.Item key={index} style={{ lineHeight: '64px' }}>
                                <Link to={`/${repo.namespace}`}>{repo.name}</Link>
                            </Menu.Item>
                        })}
                    </Menu>
                </Col>
                <Col span={2}>
                    <div style={{ lineHeight: '64px', borderBottom: '1px solid #e8e8e8' }}>
                        <a href={config.githubUrl} target="_blank" rel="noopener noreferrer">
                            <img src={github} className="github-logo" alt="logo" />
                        </a>
                    </div>
                </Col>
            </Row>
        );
    }
}

export default App;