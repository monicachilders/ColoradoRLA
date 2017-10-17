import * as React from 'react';
import { Link } from 'react-router-dom';

import LicenseFooter from 'corla/component/LicenseFooter';

import CountyNav from '../Nav';

import Info from './Info';
import Main from './Main';


interface PageProps {
    auditBoardSignedIn: boolean;
    auditComplete: boolean;
    auditStarted: boolean;
    boardSignIn: OnClick;
    canAudit: boolean;
    canRenderReport: boolean;
    canSignIn: boolean;
    contests: CountyContests;
    county: CountyState;
    countyInfo: CountyInfo;
    currentRoundNumber: number;
    startAudit: OnClick;
}

const CountyDashboardPage = (props: PageProps) => {
    const {
        auditBoardSignedIn,
        auditComplete,
        auditStarted,
        boardSignIn,
        canAudit,
        canRenderReport,
        canSignIn,
        contests,
        county,
        countyInfo,
        currentRoundNumber,
        startAudit,
    } = props;

    const auditButtonDisabled = !canAudit || auditComplete;
    const signInButtonDisabled = !canSignIn;

    return (
        <div>
            <div className='county-root'>
                <CountyNav />
                <div>
                    <Main auditComplete={ auditComplete }
                          auditStarted={ auditStarted }
                          auditBoardSignedIn={ auditBoardSignedIn }
                          boardSignIn={ boardSignIn }
                          canRenderReport={ canRenderReport }
                          county={ county }
                          currentRoundNumber={ currentRoundNumber }
                          auditButtonDisabled={ auditButtonDisabled }
                          name={ countyInfo.name }
                          signInButtonDisabled={ signInButtonDisabled }
                          startAudit={ startAudit } />
                    <Info info={ countyInfo }
                          contests={ contests }
                          county={ county }
                          currentRoundNumber={ currentRoundNumber }/>
                </div>
            </div>
            <LicenseFooter />
        </div>
    );
};


export default CountyDashboardPage;
