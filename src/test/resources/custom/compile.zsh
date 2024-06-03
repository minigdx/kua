#!/bin/zsh

# Loop through all .lua files in the current directory
for lua_file in *.lua; do
    # Extract the base name without the extension
    base_name=${lua_file:r}
    # Define the output file name with .bin extension
    output_file="${base_name}.bin"
    # Compile the Lua file to bytecode
    luac -o "$output_file" "$lua_file"
    # Check if the compilation was successful
    if [[ $? -eq 0 ]]; then
        echo "Compiled $lua_file to $output_file successfully."
    else
        echo "Failed to compile $lua_file."
    fi
done

