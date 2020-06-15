const urlRegex = new RegExp(`(https?|chrome):\/\/[^\s$.?#].[^\s]*`, 'gm')
const urlRegex1 = /(https?|chrome):\/\/[^\s$.?#].[^\s]*/gm
const str = 'https://cdn.pixabay.com/photo/2020/04/11/01/14/couple-5028352__340.jpg 1x, https://cdn.pixabay.com/photo/2020/04/11/01/14/couple-5028352__480.jpg 2x'

console.log(Array.from(str.matchAll(urlRegex1)).map(x => x[0]).find())
