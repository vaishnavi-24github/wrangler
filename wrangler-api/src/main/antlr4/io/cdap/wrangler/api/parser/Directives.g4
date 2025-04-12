// Add this rule for aggregate-size-time directive
aggregateSizeTime
    : AGGREGATE_SIZE_TIME columnName columnName columnName columnName (unit unit)? (aggregationType)?
    ;

// Add these tokens if they don't exist
AGGREGATE_SIZE_TIME
    : 'aggregate-size-time'
    ; 