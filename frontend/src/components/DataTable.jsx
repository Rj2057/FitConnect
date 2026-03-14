import { Card, EmptyState } from './ui'

export function DataTable({ columns, rows, emptyTitle, emptyDescription, onRowClick }) {
  if (!rows?.length) {
    return <EmptyState title={emptyTitle} description={emptyDescription} />
  }

  return (
    <Card className="overflow-hidden p-0">
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-[var(--line)] text-left">
          <thead className="bg-[rgba(26,31,43,0.03)]">
            <tr>
              {columns.map((column) => (
                <th key={column.key} className="px-5 py-4 text-xs font-semibold uppercase tracking-[0.2em] text-[var(--ink-soft)]">
                  {column.label}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-[var(--line)] bg-white/80">
            {rows.map((row, index) => (
              <tr
                key={row.id ?? index}
                className={onRowClick ? 'cursor-pointer hover:bg-[rgba(26,31,43,0.04)]' : ''}
                onClick={onRowClick ? () => onRowClick(row) : undefined}
              >
                {columns.map((column) => (
                  <td key={column.key} className="px-5 py-4 text-sm text-[var(--ink)]">
                    {column.render ? column.render(row) : row[column.key]}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </Card>
  )
}
