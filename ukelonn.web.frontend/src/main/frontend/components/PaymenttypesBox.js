import React from 'react';

function PaymenttypesBox(props) {
    const {id, className, paymenttypes, paymenttypesMap, value, account, paymenttype, onPaymenttypeFieldChange } = props;
    return (
        <select multiselect="true" size="10" id={id} className={className} onChange={(event) => onPaymenttypeFieldChange(event.target.value, paymenttypesMap, account)} value={value}>
          {paymenttypes.map((val) => <option key={val.id}>{val.transactionTypeName}</option>)}
        </select>
    );
}

export default PaymenttypesBox;