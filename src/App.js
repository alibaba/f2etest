import React from "react";
import { Switch, HashRouter as Router, Route, Redirect } from 'react-router-dom';

import Container from './components/Container';

import config from './config';

class App extends React.Component{
    render() {
        return (
            <Router>
                <div>
                    <Switch>
                        <Route path="/:user/:repo" component={Container} />
                        <Redirect path="/" to={{ pathname: `/${config.repos[0].namespace}` }} />
                    </Switch>
                </div>
            </Router>
        )
    }
}

export default App;