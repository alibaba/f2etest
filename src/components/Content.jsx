import React from 'react';
import Doc from '../model/doc';
import hljs from 'highlight';

class App extends React.Component {
    state = {
        match: {
            params: {
                user: {},
                repo: {},
                slug: {},
            }
        }
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

    componentDidUpdate() {
        const matches = document.querySelectorAll("pre code");

        matches.forEach((snippet) => {
            hljs.highlightBlock(snippet);
        });
    }

    render() {
        const { match } = this.state;
        const { user, repo, slug } = match.params;
        let html = '';

        if (Object.keys(user).length && Object.keys(repo).length && Object.keys(slug).length) {
            try {
                const docName = `${user}-${repo}-${slug}`;
                const docString = Doc[docName];
                const doc = JSON.parse(docString);
                html = doc['body_html'];
            } catch (e) {
                console.error(e);
            }
        }


        return <div className="flex-content">
            <div dangerouslySetInnerHTML={{ __html: html }}></div>
        </div>
    }
}

export default App;