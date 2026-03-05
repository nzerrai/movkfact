import { Box, Button, Card, CardContent, FormControl, InputLabel, MenuItem, ListSubheader, Select, Typography } from '@mui/material';
import { COLUMN_TYPE_GROUPS } from '../../constants/columnTypes';

/**
 * S2.5: TypeDetectionResults Component
 * Displays type detection results with manual override ability
 */
const TypeDetectionResults = ({
  results,
  typeOverrides,
  onTypeOverride,
  onConfirm,
  onCancel
}) => {
  const getConfidenceIcon = (confidence) => {
    if (confidence >= 80) return '🟢';
    if (confidence >= 60) return '🟡';
    return '🔴';
  };

  if (!results || results.length === 0) return null;

  const successRate =
    results.filter((r) => r.confidence > 60).length / results.length;

  return (
    <Card sx={{ mt: 3 }}>
      <CardContent>
        <Typography variant="h6" sx={{ mb: 2 }}>
          📊 Column Type Detection Results
        </Typography>

        {results.map((column) => {
          const selectedType = typeOverrides[column.name] || column.type;

          return (
            <Box
              key={column.name}
              sx={{
                display: 'flex',
                alignItems: 'center',
                gap: 2,
                padding: 2,
                borderBottom: '1px solid #eee',
                '&:hover': { backgroundColor: '#f9f9f9' }
              }}
            >
              <Typography sx={{ minWidth: 120, fontWeight: 'bold' }}>
                {column.name}
              </Typography>

              <FormControl size="small" sx={{ minWidth: 180 }}>
                <InputLabel>Type</InputLabel>
                <Select
                  value={selectedType}
                  label="Type"
                  onChange={(e) => onTypeOverride(column.name, e.target.value)}
                >
                  {COLUMN_TYPE_GROUPS.flatMap((group) => [
                    <ListSubheader key={group.label}>{group.label}</ListSubheader>,
                    ...group.types.map((t) => (
                      <MenuItem key={t.value} value={t.value}>{t.label}</MenuItem>
                    )),
                  ])}
                </Select>
              </FormControl>

              <Box
                sx={{
                  minWidth: 80,
                  textAlign: 'center',
                  display: 'flex',
                  alignItems: 'center',
                  gap: 1
                }}
              >
                <Typography variant="caption">
                  {getConfidenceIcon(column.confidence)}
                </Typography>
                <Typography variant="caption">
                  {Math.round(column.confidence)}%
                </Typography>
              </Box>

              <Typography
                variant="caption"
                color="text.secondary"
                sx={{ flex: 1 }}
              >
                {column.detector}
              </Typography>
            </Box>
          );
        })}

        <Box sx={{ mt: 2, pt: 2, borderTop: '1px solid #ddd' }}>
          <Typography variant="body2">
            📈 Success Rate: {Math.round(successRate * 100)}% (
            {results.filter((r) => r.confidence > 60).length}/{results.length}{' '}
            columns)
          </Typography>
        </Box>

        <Box
          sx={{
            display: 'flex',
            gap: 1,
            mt: 3,
            justifyContent: 'space-between'
          }}
        >
          <Button variant="outlined" onClick={onCancel}>
            Cancel
          </Button>
          <Button variant="contained" onClick={onConfirm}>
            Confirm & Continue →
          </Button>
        </Box>
      </CardContent>
    </Card>
  );
};

export default TypeDetectionResults;
