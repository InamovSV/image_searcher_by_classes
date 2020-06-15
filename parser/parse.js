const puppeteer = require("puppeteer");

async function parse(url, container = "body", tag = "img") {
    const browser = await puppeteer.launch({headless: false});
    const page = await browser.newPage();
    await page.goto(url);

    // const srcSet = Array.from(images).map(img => getImageSrc(img)).filter(Boolean);
    // await browser.close();
    const images = await page.$eval(container, node => {
        const urlRegex = /(https?|http):\/\/[^\s$.?#].[^\s]*/gm;

        function getImageSrc(tag) {

            if (tag.getAttribute("src")) {
                console.log("src: " + tag.getAttribute("src").match(urlRegex));
                return tag.getAttribute("src").match(urlRegex)
            } else {
                console.log("srcset");
                return Array.from(tag.attributes).map(attr => attr.value).find(value => {
                    return Array.from(value.matchAll(urlRegex)).map(x => x[0])[0]
                })
            }
        }

        return Array.from(node.getElementsByTagName("img")).flatMap(img => {
            return getImageSrc(img);
        }).filter(Boolean);
    });
    await browser.close();
    return images
}



module.exports = {parse};