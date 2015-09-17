import React from 'react';
import GroupIcon from '../../components/shared/GroupIcon';
import GroupsView from './groups-view';

export default React.createClass({
  render() {
    return (
        <a onClick={this.updateGroups} className="table-list-cell"href="#">
          <GroupIcon fill="#b4b4b4"/> {this.props.permission.groupsCount}
        </a>
    );
  },

  updateGroups(e) {
    e.preventDefault();
    new GroupsView({
      permission: this.props.permission.key,
      project: this.props.project,
      refresh: this.props.refresh
    }).render();
  }
});
