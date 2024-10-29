# PDF Merger
As the name suggests the project as the sole purpose of merging multiples PDF files as one. 

Usually I use [iLovePDF](https://www.ilovepdf.com/pt) to do this operation whenever I need to merge some payments receipts, but I wondered if I could do it as well. So here it goes...

## How To Use
You can use [Postman](https://www.postman.com/) to send a **POST Request** as **Form Data**. The request must contain a **files field** that should contain an **array of your files**. This should be sent to ***localhost:8080/merge-pdf*** and the response will be the merged file.

![Request URL](captures/RequestURL.png)
`Request URL`
![Request Body](captures/RequestBody.png)
`Request Body`

## Tecnologies 
- Java 17
- SpringBoot 
- Maven

### Main Dependencies 
- spring-boot-starter-web
- com.itextpdf

