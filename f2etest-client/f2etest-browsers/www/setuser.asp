<%@LANGUAGE="VBSCRIPT" CODEPAGE="65001"%><%
Response.CharSet= "UTF-8"

dim apiKey
apiKey = "" ' 请填写f2etest-web下配置的相同apiKey

dim key, username, password

key = request.QueryString("key")
username = request.QueryString("username")
password = request.QueryString("password")

if apiKey <> "" and key = apiKey and username <> "" then
    setUserPassword username,password
    if err <> 0 then
        response.write "set user failed"
    else
        response.write "ok"
    end if
else
    response.write "api error"
end if

function setUserPassword(username, password)
    On Error Resume Next
    dim oSystem,oUser,oGroup
    
    Set oSystem=GetObject("WinNT://127.0.0.1")
    
    Set oUser=oSystem.GetObject("user",username)

    if err <> 0 then
        err = 0
        ' 增加新用户
        Set oUser=oSystem.Create("user",username)
        oUser.SetPassword password
        oUser.Put "userFlags", &h10040 '密码永不过期
        oUser.Setinfo
        
        Set oGroup=oSystem.GetObject("Group","Users")
        oGroup.Add ("winnt://"&username)
    else
        ' 修改密码
        oUser.SetPassword password
        oUser.Setinfo
    end if
end function 
%>