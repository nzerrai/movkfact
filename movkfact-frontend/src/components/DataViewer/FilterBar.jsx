import React, { useState } from 'react';
import {
  Box,
  Button,
  FormControl,
  Grid,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  TextField,
  Typography,
  Chip
} from '@mui/material';
import ClearIcon from '@mui/icons-material/Clear';

/**
 * S2.7 FilterBar Component (Phase 2 Enhanced)
 * Search and filtering controls with operator support
 * Supports: contains, equals, startsWith, endsWith, >, <, >=, <=
 */
const FilterBar = ({ columns, filters, onFilterChange, onClearFilters }) => {
  const [selectedColumn, setSelectedColumn] = useState(columns[0] || '');
  const [filterValue, setFilterValue] = useState('');
  const [filterOperator, setFilterOperator] = useState('contains'); // contains, equals, startsWith, endsWith, >, <, >=, <=

  // Determine numeric operators available
  const isNumericColumn = (col) => {
    // Basic heuristic: if any cell contains 'age', 'amount', 'price', 'count', 'number'
    return /age|amount|price|count|number|quantity|total|id/i.test(col);
  };

  const numericOperators = ['equals', 'contains', '>', '<', '>=', '<='];
  const textOperators = ['contains', 'equals', 'startsWith', 'endsWith'];
  
  const availableOperators = isNumericColumn(selectedColumn) ? numericOperators : textOperators;

  // Build filter key: "columnName:operator:value"
  const buildFilterKey = (col, op, val) => `${col}:::${op}:::${val}`;
  const parseFilterKey = (key) => {
    const [col, op, val] = key.split(':::');
    return { col, op, val };
  };

  const handleAddFilter = () => {
    if (selectedColumn && filterValue) {
      const newFilters = { ...filters };
      newFilters[buildFilterKey(selectedColumn, filterOperator, filterValue)] = {
        column: selectedColumn,
        operator: filterOperator,
        value: filterValue
      };
      onFilterChange(newFilters);
      setFilterValue('');
      setFilterOperator('contains');
    }
  };

  const handleRemoveFilter = (filterKey) => {
    const newFilters = { ...filters };
    delete newFilters[filterKey];
    onFilterChange(newFilters);
  };

  const handleInputChange = (e) => {
    setFilterValue(e.target.value);
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      handleAddFilter();
    }
  };

  const getOperatorLabel = (op) => {
    const labels = {
      contains: 'Contains',
      equals: 'Equals',
      startsWith: 'Starts with',
      endsWith: 'Ends with',
      '>': 'Greater than',
      '<': 'Less than',
      '>=': 'Greater or equal',
      '<=': 'Less or equal'
    };
    return labels[op] || op;
  };

  return (
    <Box sx={{ p: 2, backgroundColor: '#fafafa', borderRadius: 1, mb: 2 }}>
      <Stack spacing={2}>
        {/* Filter Controls */}
        <Box>
          <Typography variant="subtitle2" sx={{ mb: 1 }}>
            🔍 Advanced Filter
          </Typography>
          <Grid container spacing={1}>
            <Grid item xs={12} sm={3}>
              <FormControl fullWidth size="small">
                <InputLabel>Column</InputLabel>
                <Select
                  value={selectedColumn}
                  label="Column"
                  onChange={(e) => {
                    setSelectedColumn(e.target.value);
                    setFilterOperator('contains');
                  }}
                >
                  {columns.map((col) => (
                    <MenuItem key={col} value={col}>
                      {col}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={12} sm={2}>
              <FormControl fullWidth size="small">
                <InputLabel>Operator</InputLabel>
                <Select
                  value={filterOperator}
                  label="Operator"
                  onChange={(e) => setFilterOperator(e.target.value)}
                >
                  {availableOperators.map((op) => (
                    <MenuItem key={op} value={op}>
                      {getOperatorLabel(op)}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={12} sm={3}>
              <TextField
                fullWidth
                size="small"
                placeholder="Value..."
                value={filterValue}
                onChange={handleInputChange}
                onKeyPress={handleKeyPress}
                type={isNumericColumn(selectedColumn) && ['>', '<', '>=', '<='].includes(filterOperator) ? 'number' : 'text'}
              />
            </Grid>

            <Grid item xs={12} sm={4}>
              <Stack direction="row" spacing={1}>
                <Button
                  variant="contained"
                  size="small"
                  onClick={handleAddFilter}
                  fullWidth
                >
                  Add Filter
                </Button>
                {Object.keys(filters).length > 0 && (
                  <Button
                    variant="outlined"
                    size="small"
                    startIcon={<ClearIcon />}
                    onClick={onClearFilters}
                    sx={{ whiteSpace: 'nowrap' }}
                  >
                    Clear All
                  </Button>
                )}
              </Stack>
            </Grid>
          </Grid>
        </Box>

        {/* Active Filters */}
        {Object.keys(filters).length > 0 && (
          <Box>
            <Typography variant="caption" color="textSecondary">
              Active Filters ({Object.keys(filters).length}):
            </Typography>
            <Stack direction="row" spacing={1} sx={{ mt: 1, flexWrap: 'wrap' }}>
              {Object.entries(filters).map(([filterKey, filterData]) => (
                <Chip
                  key={filterKey}
                  size="small"
                  label={`${filterData.column} ${getOperatorLabel(filterData.operator)} "${filterData.value}"`}
                  onDelete={() => handleRemoveFilter(filterKey)}
                  icon={<span>⚙️</span>}
                  sx={{
                    backgroundColor: '#e3f2fd',
                    mb: 0.5,
                    '& .MuiChip-label': { fontSize: '0.75rem' }
                  }}
                />
              ))}
            </Stack>
            <Typography variant="caption" color="success.main" sx={{ mt: 1, display: 'block' }}>
              Applying {Object.keys(filters).length} filter(s) to full dataset
            </Typography>
          </Box>
        )}
      </Stack>
    </Box>
  );
};

export default FilterBar;
