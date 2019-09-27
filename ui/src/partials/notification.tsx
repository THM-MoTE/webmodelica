import React from 'react';
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { Alert } from 'react-bootstrap'
//@ts-ignore
import { Toast } from 'react-bootstrap'
import { Notification, NotificationType } from '../models/index'
import { removeNotifications, Action } from '../redux/index';
//@ts-ignore
import Octicon from 'react-octicon'

interface Props {
  notification: Notification
  removeNotifications(n:Notification[]):void
}

export class NotificationComponentCon extends React.Component<Props, any> {

  constructor(p:Props) {
    super(p)

    //if it's an info alert; automatically remove it after 5 seconds
    if(p.notification.type === NotificationType.Info) {
      window.setTimeout(this.removeNotification.bind(this), 5000)
    }
  }

  private variant(): 'info'|'warning'|'danger' {
    switch(this.props.notification.type) {
      case NotificationType.Info: return 'info'
      case NotificationType.Warning: return 'warning'
      case NotificationType.Error: return 'danger'
    }
  }

  private removeNotification():void {
    this.props.removeNotifications([this.props.notification])
  }

  private renderAlert() {
    let octicon = ""
    switch(this.variant()) {
      case 'info' :
        octicon = 'light-bulb'
        break
      case 'warning':
        octicon = 'stop'
        break
      case 'danger':
        octicon = 'flame'
        break
    }
    return (
      <Alert variant={this.variant()} dismissible onClose={this.removeNotification.bind(this)}>
        <Octicon name={octicon} /> {this.props.notification.message}
      </Alert>
    )
  }
  private renderToast() {
    return (
      <Toast onClose={this.removeNotification.bind(this)}>
      <Toast.Header>
        <strong className="mr-auto text-success">
          <Octicon name="light-bulb" /> Info
        </strong>
      </Toast.Header>
      <Toast.Body>
        {this.props.notification.message}
      </Toast.Body>
    </Toast>
    )
  }

  render() {
    if(this.props.notification.type === NotificationType.Info)
      return this.renderToast()
    else
      return this.renderAlert()
  }
}

function dispatchToProps(dispatch: (a: Action) => any) {
  return bindActionCreators({ removeNotifications }, dispatch)
}

const NotificationComponent = connect(null, dispatchToProps)(NotificationComponentCon)
export default NotificationComponent
