const APIURL = new URL('http://localhost:8080/API/')

async function getProducts() {
    // call /API/products
    const response = await fetch(new URL('products', APIURL));
    const products = await response.json();
    if (response.ok)
        return products.map((prod) => ({
            ean: prod.ean,
            name: prod.name,
            brand: prod.brand
        }))
    else throw products;
}

async function getProductById(ean) {
    // call /API/products/:ean
    const response = await fetch(new URL('products/' + ean, APIURL));
    const product = await response.json();
    if (response.ok)
        return ({
            ean: product.ean,
            name: product.name,
            brand: product.brand
        });
    else throw product;
}

async function getProfileById(email) {
    // call /API/profiles/:email
    const response = await fetch(new URL('profiles/' + email, APIURL));
    const profile = await response.json();
    if (response.ok)
        return ({
            email: profile.email,
            firstName: profile.firstName,
            lastName: profile.lastName,
            phone: profile.phone
        });
    else throw profile;
}

function createProfile(profile) {
    // call: POST /API/profiles
    return new Promise((resolve, reject) => {
        fetch(new URL('profiles', APIURL), {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },

            body: JSON.stringify({
                email: profile.email,
                firstName: profile.firstName,
                lastName: profile.lastName,
                phone: profile.phone
            }),

        }).then((response) => {
            if (response.ok)
                resolve(response.json());
            else {
                // analyze the cause of error
                response.json()
                    .then((message) => { reject(message); }) // error message in the response body
                    .catch(() => { reject({ error: "Cannot parse server response." }) }); // something else
            }
        }).catch(() => { reject({ error: "Cannot communicate with the server." }) }); // connection errors
    });
}

function editProfile(editedProfile) {
    // call: PUT /API/profiles/:email
    return new Promise((resolve, reject) => {
        fetch(new URL('profiles/' + editedProfile.email , APIURL), {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                email: editedProfile.email,
                firstName: editedProfile.firstName,
                lastName: editedProfile.lastName,
                phone: editedProfile.phone
            })
        }).then((response) => {
            if (response.ok)
                resolve(null);
            else {
                // analyze the cause of error
                response.json()
                    .then((obj) => { reject(obj); }) // error message in the response body
                    .catch(() => { reject({ error: "Cannot parse server response." }) }); // something else
            }
        }).catch(() => { reject({ error: "Cannot communicate with the server." }) }); // connection errors
    });
}


const API = {
    getProducts, getProductById, getProfileById, createProfile, editProfile
};
export default API;