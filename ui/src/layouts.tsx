import React, { Component } from 'react';
import { Nav } from 'react-bootstrap'

export function Container(props: any) {
  return (
    <React.Fragment>
      <Nav className="justify-content-end">
        <Nav.Item><Nav.Link href="/projects">Projects</Nav.Link></Nav.Item>
        <Nav.Item><Nav.Link href="/logout">Logout</Nav.Link></Nav.Item>
      </Nav>
      <div className="container-fluid">
        {props.children}
      </div>

    </React.Fragment>
  )
}
