import React, { Component } from 'react';

export function Container(props:any) {
  return (
    <React.Fragment>
      <ul className="nav">
        <li className="nav-itm"><a className="nav-link" href="/project-view">Dashboard</a></li>
        <li className="nav-itm"><a className="nav-link" href="#">New</a></li>
      </ul>

      <div className="container-fluid">
        {props.children}
      </div>

    </React.Fragment>
  )
}
