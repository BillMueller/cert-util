**Cert-util command line tool**

*Comand line tool for genrating and reading certificates and .txt files and en-/decoding .txt files with help of the generated certificate.*

>**Important:** you get the jar by running `mvn clean install` in the cmd while you are in the folder where you extracted the compressed download. The jar file is in the targed folder that's getting created

**1 How to get it running?**   
1. Create a working directory on your PC where you want the tool to work
2. Open your cmd and switch with cd to the directory where the jar file is
3. Start the program in the cmd with `java -jar [name of the jar]`.
Now it should show something like: *[J-CONSOLE> Enter directory>*
4. Now enter your working directory for example `C:/Users/example_user/Desktop/example_working_folder`. Make sure the directory really exists and you use `/` instead of the usual `\` you use in the cmd. If you've done everything corrctly and the console is showing
something like: *[J-CONSOLE> C:/Users/example_user/Deskto/example_working_folder>*
5. Now you are ready to enter commands. If you first want to get a quick overview about all the command available enter `help`
6. Now it should show you all commands. If you want to get an information about the parameters you can use them with type the command you want to know the parameters together with -h or --help for example `writeCertificate --help`

**2 How to use the commands?**   
You can run all the commands you can see if you enter the `help` command. Some of them need some extra parameters and some of them run without any additional information. To find out if you need parameters just enter the command without any parameters. If the command
needs parameters it will let you know. For example if you type in `help`, the console will show you *[ERROR] you have to enter a file name with the argument --file <filename>*. If command will show an error message read the message and try to find out what the problem
is. Sometime the console also will give you an advice what you did wrong. For example if you're entering a directory and you use `C:\Users` it will show you *[ERROR] the path file you entered is not valid* and in the next line a advice *[INFO] Use '/' for example C:/Users*.

>**Important:** use the `exit` command to exit the console

**3 For what can you use the J-Console command line tool?**

**3.1 encoding messages**   
*If you for example want to encode a message for your friend:*
* both generate a certificate with `wc --file your_certificate` - **Don't use the same name!** -> optionally add the parameter --sName [your name] so you can later see who is the owner of the certificate
* exchange your certificates - **Not the private key** - and put your friend's certificate into your working directory
* put your message into a .txt file and insert the file into the working directory. The working directory now has to include: *message.txt* | *your_friends_certificate.crt*
* now you can use the `ed --file message --certFile your_firends_certificate` command - *message has to be the .txt file name and your_friends_certificate has to be the name your friend used to generate his certificate*
* at this point the message is encoded and you can send it to your friend

**3.2 decoding messages**   
*If you for example want to decode an encoded message you got from your friend*  
>**Important:** the friend has to use **your** certificate for encoding
* put your friends encoded message as .txt file into your working directory. The working directory now has to include: *message.txt*  | *your_certificate_private_key*
* now you can use the `dd --file message --certFile your_certificate` command - *message has to be the .txt file name and your_certificate has to be the name you used to generate your certificate*
* at this point the message is decoded and you can read the message

**3.3 read a certificate**   
*If you need some information like the validity or the owner of the certificate*
* use the command `rc --file the_certificate_you_want_to_read`
* now the console will print out all certificate information

**3.4 write and read .txt documents**   
*If you want to write or read messages without using an external editor*

**3.4.1 write a .txt document**   
>**Important:** you need to think of the number of lines before you execute the command to call the internal writer
* use the command `wd --file your_file_name [--replace]` (the --replace parameter will replace the old text in the file and is optional)
* you will see a line number and you can write
* use "enter" to go to the next line
* the editor will return to console after you wrote the number of lines you chose at the start

**3.4.2 read a .txt document**   
* use the command `rd --file your_file_name`
* the editor will print out the file

**4 Additional commands**   
setConfig  / sc [changes the config file]
* --file [the new directory of the config.properties file]
* --copyConfig - *add this argument to copy the default config file to your new directory* `optional`

changeDirectory / cd [changes back to the "Enter Directory" input line]

changeStyle / cs [changes the console style]
* --toggle - toggles to the next style `optional`
* --style [the style you want the console to have] `optional` - if you leave that parameter away the console will use the default style    
**the styles available:**
    * default / d - the default style
    * non-colored / nc - the default style without color
    * one-colored / oc - the default style with only one color (yellow)
    * one-lettered / ol - default style but with only one letter (for example [H] instead of [HELP])
    * simple / s - default style but with only symbol (for example [*] instead of [HELP])

exit [exits the console]
