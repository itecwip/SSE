var runOnAndroid = true;

function loadURL(url) 
{
	window.location = url;
}

function gotoURL(url) 
{
	if (runOnAndroid) window.SJSI.gotoURL(url);
	else window.location = url;
}

function gotoAndRoll(url, id)
{
	if (runOnAndroid) window.SJSI.gotoURL(url, id);
	else window.location = url + "#" + id;
}

function getValue(key)
{
	if (runOnAndroid) return window.SJSI.getValue(key);
	else return "null";
}

function scrollToElement(id, offsetTop) 
{
	var elem = document.getElementById(id);
    var x = 0;
    var y = 0;
	var docHeight = 0;
	if (offsetTop == null) offsetTop = 0;

	if (self.innerHeight)
		docHeight = window.innerHeight
	else if (document.documentElement && document.documentElement.clientHeight)
		docHeight = document.documentElement.clientHeight;
	else if (document.body)
		docHeight = document.body.clientHeight;

	if (docHeight == null || docHeight < 1) return false;

    if (!isNaN(id)) x = id;
	else {
		while (elem != null) {
			x += elem.offsetLeft;
			y += elem.offsetTop;
			elem = elem.offsetParent;
		}
	}

	if (runOnAndroid) window.SJSI.scrollToXY(x, y + offsetTop, docHeight);
	else window.scroll(x, y);
}

function setSignElement(id, userId, domain)
{
	document.getElementById(id).innerHTML = userId + "@" + domain;
}

function sendMessageAndClose(message)
{
	if (runOnAndroid) window.SJSI.sendMessageAndClose(message);
}

function formatPage()
{
	if (runOnAndroid) {
		try	{
			document.getElementById("toMainMenu").style.display = 'none';
		} catch(err){};
	}
	
	try	{
		document.getElementById("reserveSpace").style.display = 'none';
	} catch(err){};
}

function showHand(element)
{
	element.style.cursor='hand'; 
	element.style.cursor='pointer';
}
