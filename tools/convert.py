#!/usr/bin/python3

from email import header
import sys, os, getopt

def find_divider(dividers, row):
    if len(dividers) == 1:
        return dividers
    # if more than one, try which splits best
    div = None
    max_parts = 0
    for x in range(len(dividers)):
        parts = row.split(dividers[x])
        if len(parts) > max_parts:
            max_parts = len(parts)
            div = dividers[x]
    return div

def print_usage(exit_code):
    print("usage: convert.py -i <input_files> [ -n <num_header_rows> -d <dividers> -t " + \
          "<table_name> -f <default_column_family_name> -c <column_names> -k <row_key_col> -x -l -v ]")
    sys.exit(exit_code)    

def main(argv):
    # Set defaults
    verbose = False
    input_files = []
    num_header_rows = 0
    dividers = ",|\t"
    table_name = None
    colfam_name = None
    column_names = []
    row_key_col = None
    drop_table = False
    lower_case = False

    # Parse command-line arguments
    try:
        opts, args = getopt.getopt(argv,"hi:n:d:t:f:c:k:xlv",
            ["input_files=","num_header_rows","dividers=","table_name=",
             "default_column_family_name=","column_names=","row_key_col="
             "drop_table","lower_case","verbose"])
    except getopt.GetoptError:
        print_usage(2)
    for opt, arg in opts:
        if opt == '-h':
            print_usage(0)
        elif opt in ("-i", "--input_files"):
            input_files = arg.split(',')
        elif opt in ("-n", "--num_header_rows"):
            num_header_rows = int(arg)
        elif opt in ("-d", "--dividers"):
            dividers = arg
        elif opt in ("-t", "--table_name"):
            table_name = arg
        elif opt in ("-f", "--default_column_family_name"):
            colfam_name = arg
        elif opt in ("-c", "--column_names"):
            column_names = arg.split(',')
        elif opt in ("-k", "--row_key_col"):
            row_key_col = arg
        elif opt in ("-x", "--drop_table"):
            drop_table = True
        elif opt in ("-l", "--lower_case"):
            lower_case = True
        elif opt in ("-v", "--verbose"):
            verbose = True

    # Verify args
    if len(input_files) < 1:
        print("ERROR: Missing parameters!")
        print_usage(1)

    # Iterate over files
    for input_file in input_files:
        if verbose:
            print("# Processing file:", input_file)
        # Determine table names and other details
        tbl_name = table_name
        if not tbl_name:
            tbl_name = os.path.basename(input_file).split('.')[0]
        if lower_case:
            tbl_name = tbl_name.lower()
        # Emit optional header lines 
        if drop_table:
            print('disable "%s"' % tbl_name)
            print('drop "%s"' % tbl_name)
        # Open file and read content
        with open(input_file, 'r') as file_handle:
            # Determine column families
            header_rows = []
            colfams = []
            # Optionally read header rows
            if num_header_rows > 0:
                for i in range(num_header_rows):
                    header_rows.append(file_handle.readline().rstrip('\n'))
            # Check if columns are named explicitly
            if len(column_names) > 0:
                # For example: [ "m:id", "d:name", "d:age", ... ]
                cols = column_names
            elif len(header_rows) > 0:
                # Assume the header row(s) contain the names
                # For example: id|name|age|... -> [ "id", "name", "age", ... ]
                divider = find_divider(dividers, header_rows[0])
                cols = divider.join(header_rows)
                cols = cols.split(divider)
            else:
                raise AssertionError('Must provide header rows or explicit column names!')
            # Backfill column families, also determine (optional) row_key ID
            cf_cols = []
            col_index = 0
            row_key_id = -1
            for col in cols:
                # Split and set both column family and column name
                parts = col.split(':')
                cf = colfam_name
                co = col
                if len(parts) == 2:
                    cf = parts[0]
                    co = parts[1]
                # Remember both column families and well as full column names
                if cf not in colfams:
                    colfams.append(cf)
                cf_co = "%s:%s" % (cf, co)
                cf_cols.append(cf_co)
                # Determine the row key ID
                if row_key_id == -1 and row_key_col == col:
                    row_key_id = col_index
                col_index += 1
            # Emit create statement
            cfs = ', '.join(['"%s"' % ele for ele in colfams])
            print('create "%s", %s' % (tbl_name, cfs))
            # Iterate over rows
            row_string = 'put "%s", "%s", "%s", "%s"'
            row_count = 0
            while True:
                # Read a single row, exit if none is left
                row = file_handle.readline().rstrip('\n')
                if not row:
                    break;
                # Split columns on explicit or implicit divider
                if not divider:
                    divider = find_divider(dividers, row)
                row_cols = row.split(divider)
                # Determine row key, default is row counter value
                row_key = row_count
                if row_key_id > -1:
                    row_key = row_cols[row_key_id]
                # Iterate over columns and emit PUT statements
                row_col_count = 0
                skipped_count = 0
                for row_col in row_cols:
                    row_col = row_col.strip()
                    put_cmd = row_string % (tbl_name, row_key, cf_cols[row_col_count], row_col)
                    if len(row_col) > 0:
                        print(put_cmd)
                    elif verbose:
                        print("# Skipping empty value!")
                        print("#", put_cmd)
                        skipped_count += 1
                    row_col_count += 1
                row_count += 1
            if verbose:
                print("# Row processed:", row_count, " - Skipped puts:", skipped_count)
    if verbose:
        print("# Complete file:", input_file)

# Main entrypoint
if __name__ == "__main__":
    main(sys.argv[1:])

