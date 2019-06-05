import React, { Component } from 'react'
import { UserSession } from 'blockstack'
import { appConfig } from './constants'
import styles from './Landing.css'

class Landing extends Component {

  constructor() {
    super()
    this.userSession = new UserSession({ appConfig })
  }

  signIn(e) {
    e.preventDefault()
    this.userSession.redirectToSignIn()
  }

  render() {
    return (
      <div className="Landing">
        <h1 className="h1 mb-3 font-weight-normal"></h1>
        <div className="form-signin">
          <button
            className="btn btn-lg btn-primary btn-block"
            onClick={this.signIn.bind(this)}>Sign in with Blockstack
          </button>
        </div>
      </div>
    );
  }
}

export default Landing
