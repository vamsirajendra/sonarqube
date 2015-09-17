import React from 'react';
import UserIcon from '../../components/shared/UserIcon';
import UsersView from './users-view';

export default React.createClass({

  render() {
    return (
        <a onClick={this.updateUsers} className="table-list-cell" href="#">
          <UserIcon fill="#b4b4b4"/> {this.props.permission.usersCount}
        </a>
    );
  },

  updateUsers(e) {
    e.preventDefault();
    new UsersView({
      permission: this.props.permission.key,
      project: this.props.project,
      refresh: this.props.refresh
    }).render();
  }
});
