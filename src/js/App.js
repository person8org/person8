import React, { Component } from 'react'
import styles from './App.css'
import { UserSession } from 'blockstack'

import Landing from './Landing'
import SignedIn from './SignedIn'

class App extends Component {

  constructor(props) {
    super(props)
    this.userSession = new UserSession()
    console.log("Props=", this.props)
  }

  componentWillMount() {
    const session = this.userSession
    if (this.props.dispatch) {
      this.props.dispatch("UserSession", session)
    }
    if(!session.isUserSignedIn() && session.isSignInPending()) {
      session.handlePendingSignIn()
      .then((userData) => {
        if(!userData.username) {
          throw new Error('This app requires a username.')
        }
        if (this.props.dispatch) {
          this.props.dispatch("UserData", userData)
        }
      })
    }
  }

  render() {
    return (
      <main role="main">
          {this.userSession.isUserSignedIn() ?
            <div>
              {this.props.children}
              <SignedIn />
           </div>
          :
            <Landing />
          }
      </main>
    );
  }
}

export default App
