import * as React from 'react';

import { EditableText } from '@blueprintjs/core';


class CVRUploader extends React.Component<any, any> {
    public state: any = {
        file: null,
        hash: '',
    };

    public render() {
        const { forms, upload } = this.props;
        const { file, hash } = this.state;

        forms.cvrExportForm = this.state;

        const fileName = file ? file.name : '';

        return (
            <div className='pt-card'>
                <div className='pt-card'>
                    <div>
                        Cast Vote Record Export file
                    </div>
                    <label className='pt-file-upload'>
                        <input type='file' onChange={ this.onFileChange } />
                        <span className='pt-file-upload-input'>{ fileName }</span>
                    </label>
                </div>
                <div className='pt-card'>
                    <div>
                        SHA-256 hash for Cast Vote Record Export file
                    </div>
                    <label>
                        <EditableText
                            minWidth={ 500 }
                            maxLength={ 64 }
                            className='pt-input'
                            value={ hash }
                            onChange={ this.onHashChange } />
                    </label>
                </div>
                <button className='pt-button' onClick={ upload }>
                    Upload
                </button>
            </div>
        );
    }

    public onFileChange = (e: any) => {
        const s = { ...this.state };

        s.file = e.target.files[0];

        this.setState(s);
    }

    public onHashChange = (hash: any) => {
        const s = { ...this.state };

        s.hash = hash;

        this.setState(s);
    }
}


export default CVRUploader;
