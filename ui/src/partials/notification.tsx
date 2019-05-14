import React from 'react';
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { Alert } from 'react-bootstrap'

import { Notification, NotificationType } from '../models/index'
import { removeNotifications, Action } from '../redux/index';

interface Props {
  notification: Notification
  removeNotifications(n:Notification[]):void
}

export class NotificationComponentCon extends React.Component<Props, any> {

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

  render() {
    return (
      <Alert variant={this.variant()} dismissible onClose={this.removeNotification.bind(this)}>
        {this.props.notification.message}
      </Alert>
    )
  }
}

function dispatchToProps(dispatch: (a: Action) => any) {
  return bindActionCreators({ removeNotifications }, dispatch)
}

const NotificationComponent = connect(null, dispatchToProps)(NotificationComponentCon)
export default NotificationComponent
