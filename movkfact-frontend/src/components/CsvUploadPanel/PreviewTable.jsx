import { Box } from '@mui/material';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Typography
} from '@mui/material';

/**
 * S2.5: PreviewTable Component
 * Displays first 10 rows of CSV data
 */
const PreviewTable = ({ csvData, title = 'Preview (first 10 rows)' }) => {
  if (!csvData || csvData.length === 0) return null;

  const columnNames = Object.keys(csvData[0]);
  const rows = csvData.slice(0, 10);

  return (
    <Box sx={{ mt: 3 }}>
      <Typography variant="h6" sx={{ mb: 2 }}>
        {title}
      </Typography>
      <TableContainer component={Paper}>
        <Table size="small" dense>
          <TableHead>
            <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
              <TableCell align="center" sx={{ fontWeight: 'bold' }}>
                #
              </TableCell>
              {columnNames.map((col) => (
                <TableCell key={col} sx={{ fontWeight: 'bold' }}>
                  {col}
                </TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map((row, idx) => (
              <TableRow key={idx} hover>
                <TableCell align="center" sx={{ backgroundColor: '#fafafa' }}>
                  {idx + 1}
                </TableCell>
                {columnNames.map((col) => (
                  <TableCell
                    key={`${idx}-${col}`}
                    sx={{
                      maxWidth: 150,
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      whiteSpace: 'nowrap'
                    }}
                    title={String(row[col])}
                  >
                    {String(row[col]).substring(0, 50)}
                  </TableCell>
                ))}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
};

export default PreviewTable;
