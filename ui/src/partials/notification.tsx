import React from 'react';
import { Notification} from '../models/index'

interface Props {
  notification: Notification
}

export class NotificationComponent extends React.Component<Props, any> {

  componentDidMount() {

  }
  render() {
    return (
      <div className="toast" role="alert" aria-live="assertive" aria-atomic="true">
        <div className="toast-header">
          <img src="..." className="rounded mr-2" alt="..." />
            <strong className="mr-auto">Webmodelica</strong>
            <small>11 mins ago</small>
            <button type="button" className="ml-2 mb-1 close" data-dismiss="toast" aria-label="Close">
              <span aria-hidden="true">&times;</span>
            </button>
        </div>
        <div className="toast-body">
            {this.props.notification.message}
        </div>
      </div>
    )
  }
}
