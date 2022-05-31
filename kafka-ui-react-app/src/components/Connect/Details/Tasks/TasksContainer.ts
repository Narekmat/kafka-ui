import { connect } from 'react-redux';
import { RootState } from 'redux/interfaces';
import { fetchConnectorTasks } from 'redux/reducers/connect/connectSlice';
import {
  getAreConnectorTasksFetching,
  getConnectorTasks,
} from 'redux/reducers/connect/selectors';

import Tasks from './Tasks';

const mapStateToProps = (state: RootState) => ({
  areTasksFetching: getAreConnectorTasksFetching(state),
  tasks: getConnectorTasks(state),
});

const mapDispatchToProps = {
  fetchTasks: fetchConnectorTasks,
};

export default connect(mapStateToProps, mapDispatchToProps)(Tasks);
