import React from 'react';
import {
  Box,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TablePagination,
  TableRow,
  Tooltip,
  Typography
} from '@mui/material';
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp';
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';

/**
 * S2.7 DataTable Component
 * Display dataset in table format with sorting and pagination
 */
const DataTable = ({
  data,
  columns,
  sortConfig,
  onSort,
  pageIndex,
  rowsPerPage,
  totalRows,
  onPageChange,
  onRowsPerPageChange,
  totalPages
}) => {
  const handleSort = (column) => {
    const event = window.lastMouseEvent;
    const isShiftClick = event?.shiftKey || false;
    onSort(column, isShiftClick);
  };

  // Get sort info for a column (for multi-column sort display)
  const getSortInfo = (column) => {
    if (!sortConfig?.columns) return null;
    const sortItem = sortConfig.columns.find(s => s.column === column);
    return sortItem;
  };

  const handleChangePage = (event, newPage) => {
    onPageChange(newPage);
  };

  const handleChangeRowsPerPage = (event) => {
    onRowsPerPageChange(parseInt(event.target.value, 10));
  };

  if (!columns || columns.length === 0) {
    return (
      <Paper sx={{ p: 3, textAlign: 'center' }}>
        <Typography color="textSecondary">No data available</Typography>
      </Paper>
    );
  }

  const truncate = (value, maxLen = 50) => {
    const str = String(value || '');
    return str.length > maxLen ? str.slice(0, maxLen) + '...' : str;
  };

  return (
    <Paper>
      <Box sx={{ overflowX: 'auto' }}>
        <Table size="small" stickyHeader>
          <TableHead>
            <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
              {columns.map((col) => {
                const sortInfo = getSortInfo(col);
                const sortIndex = sortConfig?.columns?.findIndex(s => s.column === col) ?? -1;
                
                return (
                  <TableCell
                    key={col}
                    onClick={(e) => {
                      window.lastMouseEvent = e;
                      handleSort(col);
                    }}
                    sx={{
                      fontWeight: 600,
                      cursor: 'pointer',
                      userSelect: 'none',
                      backgroundColor: sortInfo ? '#e8f4f8' : 'inherit',
                      '&:hover': {
                        backgroundColor: '#eeeeee'
                      }
                    }}
                    title={sortInfo ? `Sorted ${sortInfo.direction} (click to toggle, shift+click for multi-sort)` : 'Click to sort, shift+click for secondary sort'}
                  >
                    <Stack direction="row" alignItems="center" spacing={0.5}>
                      <span>{col}</span>
                      {sortInfo && (
                        <Stack direction="row" alignItems="center" spacing={0.25}>
                          {sortInfo.direction === 'asc' ? (
                            <KeyboardArrowUpIcon fontSize="small" />
                          ) : (
                            <KeyboardArrowDownIcon fontSize="small" />
                          )}
                          {sortIndex !== -1 && sortConfig.columns.length > 1 && (
                            <Typography variant="caption" sx={{ fontWeight: 'bold', color: '#1976d2' }}>
                              {sortIndex + 1}
                            </Typography>
                          )}
                        </Stack>
                      )}
                    </Stack>
                  </TableCell>
                );
              })}
            </TableRow>
          </TableHead>
          <TableBody>
            {data.length > 0 ? (
              data.map((row, rowIdx) => (
                <TableRow
                  key={rowIdx}
                  sx={{
                    '&:hover': { backgroundColor: '#fafafa' },
                    '&:nth-of-type(even)': { backgroundColor: '#ffffff' }
                  }}
                >
                  {columns.map((col) => {
                    const value = row[col];
                    const isNull = value === null || value === undefined || value === '';
                    return (
                      <TableCell
                        key={`${rowIdx}-${col}`}
                        sx={{
                          backgroundColor: isNull ? '#f5f5f5' : 'inherit',
                          color: isNull ? '#999' : 'inherit'
                        }}
                      >
                        <Tooltip title={String(value || 'null')}>
                          <span>{truncate(value || 'null', 50)}</span>
                        </Tooltip>
                      </TableCell>
                    );
                  })}
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={columns.length} align="center" sx={{ py: 3 }}>
                  <Typography color="textSecondary">
                    No rows to display
                  </Typography>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </Box>

      {/* Pagination */}
      <TablePagination
        component="div"
        count={totalRows}
        page={pageIndex}
        onPageChange={handleChangePage}
        rowsPerPage={rowsPerPage}
        onRowsPerPageChange={handleChangeRowsPerPage}
        rowsPerPageOptions={[25, 50, 100, 250, 500]}
      />
    </Paper>
  );
};

export default DataTable;
