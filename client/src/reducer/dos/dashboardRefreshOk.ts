import { merge } from 'lodash';

import { parse } from 'corla/adapter/dosDashboardRefresh';


export default (state: AppState, action: any): AppState => {
    const nextState = { ...state };

    const sos = merge({}, parse(action.data));
    nextState.sos = merge({}, nextState.sos, sos);
    nextState.sos.auditedContests = sos.auditedContests;
    nextState.sos.countyStatus = sos.countyStatus;

    return nextState;
};
