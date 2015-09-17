import React from 'react';
import UsersView from './users-view';
import GroupsView from './groups-view';
import ApplyTemplateView from './apply-template-view';
import UserIcon from '../../components/shared/UserIcon';
import GroupIcon from '../../components/shared/GroupIcon';
import {getProjectUrl} from '../../helpers/Url';

export default React.createClass({
  propTypes: {
    project: React.PropTypes.object.isRequired,
    permissionTemplates: React.PropTypes.arrayOf(React.PropTypes.object).isRequired,
    refresh: React.PropTypes.func.isRequired
  },

  showGroups(permission, e) {
    e.preventDefault();
    new GroupsView({
      permission: permission,
      project: this.props.project.id,
      projectName: this.props.project.name,
      refresh: this.props.refresh
    }).render();
  },

  showUsers(permission, e) {
    e.preventDefault();
    new UsersView({
      permission: permission,
      project: this.props.project.id,
      projectName: this.props.project.name,
      refresh: this.props.refresh
    }).render();
  },

  applyTemplate(e) {
    e.preventDefault();
    new ApplyTemplateView({
      permissionTemplates: this.props.permissionTemplates,
      project: this.props.project,
      refresh: this.props.refresh
    }).render();
  },

  render() {
    let permissions = [];
    this.props.project.permissions.forEach(p => {
      permissions.push(
          <td key={'users-' + p.key} className="text-center">
            <a onClick={this.showUsers.bind(this, p.key)} className="table-list-cell" href="#">
              <UserIcon fill="#b4b4b4"/> {p.usersCount}
            </a>
          </td>
      );
      permissions.push(
          <td key={'groups-' + p.key} className="text-center">
            <a onClick={this.showGroups.bind(this, p.key)} className="table-list-cell" href="#">
              <GroupIcon fill="#b4b4b4"/> {p.groupsCount}
            </a>
          </td>
      );
    });

    return (
        <tr>
          <td>
            <strong>
              <a href={getProjectUrl(this.props.project.key)}>{this.props.project.name}</a>
            </strong>
          </td>
          {permissions}
          <td className="thin nowrap text-right big-spacer-left">
            <button onClick={this.applyTemplate} className="js-apply-template">Apply Template</button>
          </td>
        </tr>
    );
  }
});
