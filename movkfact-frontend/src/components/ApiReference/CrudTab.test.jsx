import React from 'react';
import { render, screen } from '@testing-library/react';
import CrudTab from './CrudTab';

jest.mock('./EndpointCard', () => ({ endpoint, defaultDomainId, defaultDatasetId }) => (
  <div data-testid="endpoint-card" data-method={endpoint.method} data-path={endpoint.path}>
    {endpoint.method} {endpoint.path} domainId={defaultDomainId} datasetId={defaultDatasetId}
  </div>
));

describe('CrudTab', () => {
  it('renders 8 EndpointCards', () => {
    render(<CrudTab defaultDomainId={1} defaultDatasetId={42} />);
    expect(screen.getAllByTestId('endpoint-card')).toHaveLength(8);
  });

  it('passes defaultDomainId to each card', () => {
    render(<CrudTab defaultDomainId={7} defaultDatasetId={null} />);
    const cards = screen.getAllByText(/domainId=7/);
    expect(cards.length).toBeGreaterThan(0);
  });

  it('passes defaultDatasetId to each card', () => {
    render(<CrudTab defaultDomainId={1} defaultDatasetId={99} />);
    const cards = screen.getAllByText(/datasetId=99/);
    expect(cards.length).toBeGreaterThan(0);
  });

  it('includes all expected methods', () => {
    render(<CrudTab defaultDomainId={1} defaultDatasetId={1} />);
    const cards = screen.getAllByTestId('endpoint-card');
    const methods = cards.map(c => c.getAttribute('data-method'));
    expect(methods.filter(m => m === 'GET').length).toBe(4);
    expect(methods.filter(m => m === 'POST').length).toBe(2);
    expect(methods.filter(m => m === 'DELETE').length).toBe(2);
  });

  it('includes /api/data-sets/{id}/rows endpoint', () => {
    render(<CrudTab defaultDomainId={1} defaultDatasetId={1} />);
    const card = screen.getByText(/\/api\/data-sets\/\{id\}\/rows/);
    expect(card).toBeInTheDocument();
  });
});
