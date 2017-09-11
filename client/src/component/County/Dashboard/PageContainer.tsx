import * as React from 'react';
import { connect } from 'react-redux';

import counties from 'corla/data/counties';

import CountyDashboardPage from './Page';

import finishAudit from 'corla/action/county/finishAudit';

import allRoundsCompleteSelector from 'corla/selector/county/allRoundsComplete';
import auditBoardSignedInSelector from 'corla/selector/county/auditBoardSignedIn';
import auditCompleteSelector from 'corla/selector/county/auditComplete';
import auditStartedSelector from 'corla/selector/county/auditStarted';
import canAuditSelector from 'corla/selector/county/canAudit';
import canRenderReportSelector from 'corla/selector/county/canRenderReport';
import canSignInSelector from 'corla/selector/county/canSignIn';


class CountyDashboardContainer extends React.Component<any, any> {
    public render() {
        const {
            allRoundsComplete,
            auditStarted,
            canAudit,
            canRenderReport,
            canSignIn,
            county,
            history,
        } = this.props;

        const countyInfo = county.id ? counties[county.id] : {};
        const boardSignIn = () => history.push('/county/sign-in');
        const startAudit = () => history.push('/county/audit');

        const props = {
            allRoundsComplete,
            auditStarted,
            boardSignIn,
            canAudit,
            canRenderReport,
            canSignIn,
            countyInfo,
            finishAudit,
            startAudit,
            ...this.props,
        };

        return <CountyDashboardPage { ...props } />;
    }
}

const mapStateToProps = (state: any) => {
    const { county } = state;
    const { contestDefs } = county;

    return {
        allRoundsComplete: allRoundsCompleteSelector(state),
        auditBoardSignedIn: auditBoardSignedInSelector(state),
        auditComplete: auditCompleteSelector(state),
        auditStarted: auditStartedSelector(state),
        canAudit: canAuditSelector(state),
        canRenderReport: canRenderReportSelector(state),
        canSignIn: canSignInSelector(state),
        contests: contestDefs,
        county,
    };
};

export default connect(mapStateToProps)(CountyDashboardContainer);
