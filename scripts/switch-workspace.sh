 env=$1

 if [[ -z $(terraform workspace list | grep -o $env) ]];
 then 
    terraform workspace new $env
 else 
    terraform workspace select $env
 fi