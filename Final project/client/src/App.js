import './App.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import { useEffect, useState } from 'react';
import { Button, Container, Row, Col } from 'react-bootstrap';
import { House, BoxArrowLeft, Person } from 'react-bootstrap-icons';
import { BrowserRouter as Router, Routes, Route, Navigate, useNavigate, Outlet } from 'react-router-dom';
import { LoginPage } from "./login/LoginPage";
import { FormModifyProfile } from "./FormModifyProfile";
import CustomerHomePage from './customer/CustomerHomePage';
import ExpertHomePage from './expert/ExpertHomePage';
import ManagerHomePage from './manager/ManagerHomePage';
import TicketPage from './customer/TicketPage';
import PurchasePage from './customer/PurchasePage';
import ExpertProfilePage from './manager/ExpertProfilePage';
import NewPurchaseForm from "./customer/NewPurchaseForm";
import { FormCreateTicket } from "./FormCreateTicket";
import API from './API';
import jwt_decode from "jwt-decode";

function App() {
  return (
    <Router>
      <App2 />
    </Router>
  );
}
function App2() {
  const [loggedIn, setLoggedIn] = useState(false);
  const [userId, setUserId] = useState('');
  const [username, setUsername] = useState('');
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [role, setRole] = useState('');
  const [message, setMessage] = useState('');
  const [dirty, setDirty] = useState(false);

  const navigate = useNavigate();

  // log the errors
  useEffect(() => {
    if (message !== '') {
      console.log(message);
    }
  }, [message])

  // on start check if access token is still saved (so, still logged in)
  useEffect(() => {
    let mustRefreshToken = true;
    const accessToken = localStorage.getItem('accessToken');

    if (accessToken !== '' && accessToken !== null) {
      const accessTokenExpirationTime = jwt_decode(accessToken).exp * 1000;

      if (accessTokenExpirationTime > new Date().getTime()) {
        mustRefreshToken = false;

        const userId = jwt_decode(accessToken).sub;
        const username = jwt_decode(accessToken).preferred_username;
        const name = jwt_decode(accessToken).name;
        const email = jwt_decode(accessToken).email;
        const role = jwt_decode(accessToken).resource_access['wa2-products-client'].roles[0];
        setUserId(userId);
        setUsername(username);
        setName(name);
        setEmail(email);
        setRole(role);
        setLoggedIn(true);
        setMessage('');
        setTimeout(doRefresh, accessTokenExpirationTime - new Date().getTime());
      }
    }

    if (mustRefreshToken) { // true if accessToken is null or expired
      doRefresh();
    }
  }, [])

  function doLogin(email, password) {
    API.login(email, password)
      .then(jwtDTO => {
        localStorage.setItem('accessToken', jwtDTO.accessToken);
        localStorage.setItem('refreshToken', jwtDTO.refreshToken);
        setTimeout(doRefresh, jwt_decode(jwtDTO.accessToken).exp * 1000 - new Date().getTime());

        const userId = jwt_decode(jwtDTO.accessToken).sub;
        const username = jwt_decode(jwtDTO.accessToken).preferred_username;
        const name = jwt_decode(jwtDTO.accessToken).name;
        const email = jwt_decode(jwtDTO.accessToken).email;
        const role = jwt_decode(jwtDTO.accessToken).resource_access['wa2-products-client'].roles[0];
        setUserId(userId);
        setUsername(username);
        setName(name);
        setEmail(email);
        setRole(role);
        setMessage('');
        setLoggedIn(true);
        navigate('/');
      })
      .catch(err => {
        if (err.status === 400)
          setMessage('Incorrect email and/or password.')
        else
          setMessage(err.detail);
      })
  }

  function doRefresh() {
    let mustLogout = true;
    const refreshToken = localStorage.getItem('refreshToken');

    if (refreshToken !== '' && refreshToken !== null) {
      const refreshTokenExpirationTime = jwt_decode(refreshToken).exp * 1000;

      if (refreshTokenExpirationTime > new Date().getTime()) {
        mustLogout = false;

        API.refreshLogin(refreshToken)
          .then(jwtDTO => {
            localStorage.setItem('accessToken', jwtDTO.accessToken);
            localStorage.setItem('refreshToken', jwtDTO.refreshToken);
            setTimeout(doRefresh, jwt_decode(jwtDTO.accessToken).exp * 1000 - new Date().getTime());

            const userId = jwt_decode(jwtDTO.accessToken).sub;
            const username = jwt_decode(jwtDTO.accessToken).preferred_username;
            const name = jwt_decode(jwtDTO.accessToken).name;
            const email = jwt_decode(jwtDTO.accessToken).email;
            const role = jwt_decode(jwtDTO.accessToken).resource_access['wa2-products-client'].roles[0];
            setUserId(userId);
            setUsername(username);
            setName(name);
            setEmail(email);
            setRole(role);
            setMessage('');
            setLoggedIn(true);
          })
          .catch(err => {
            setMessage(err.detail);
          })
      }
    }

    if (mustLogout) { // true if refreshToken is null or expired
      doLogout();
    }
  }

  function doLogout() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    setUserId('');
    setUsername('');
    setName('');
    setEmail('');
    setRole('');
    setMessage('');
    setLoggedIn(false);
  }

  function handleError(err) {
    console.log(err);
  }

  return (
    <Routes>
      <Route path='/login' element={loggedIn ? <Navigate to='/' /> : <LoginPage loggedIn={loggedIn} doLogin={doLogin} message={message} setMessage={setMessage} />} />
      <Route path='/' element={loggedIn ? <PageLayout loggedIn={loggedIn} doLogin={doLogin} doLogout={doLogout} email={email} name={name} role={role} /> : <Navigate to='/login' />}>
        <Route index element={role === 'customer' ? <CustomerHomePage name={name} dirty={dirty} setDirty={setDirty} /> :
          role === 'expert' ? <ExpertHomePage name={name} /> :
            <ManagerHomePage name={name} />} />
        <Route path='ticket/:ticketId' element={<TicketPage userId={userId} email={email} role={role} dirty={dirty} setDirty={setDirty} />} />
        <Route path='purchase/:purchaseId' element={<PurchasePage email={email} role={role} dirty={dirty} setDirty={setDirty} />} />
        <Route path='new-ticket/:purchaseId' element={<FormCreateTicket dirty={dirty} setDirty={setDirty} handleError={handleError} />} />
        <Route path='new-purchase' element={<NewPurchaseForm dirty={dirty} setDirty={setDirty} email={email} role={role} handleError={handleError} />} />
        <Route path='profile/:email' element={<FormModifyProfile dirty={dirty} setDirty={setDirty} email={email} role={role} setName={setName} handleError={handleError} />} />
        <Route path='expert/:expertId' element={<ExpertProfilePage />} />
      </Route>

      <Route path='*' element={<Navigate to='/' />} />
    </Routes>
  );
}

function PageLayout(props) {
  const navigate = useNavigate();

  return (
    <Container>
      <Row className='navbar'>
        <Col>
          <Button variant='light' className='navbar-button' onClick={() => navigate('/')}>
            <span className='d-flex justify-content-center align-items-center'><House />Home</span>
          </Button>
        </Col>
        <Col>
          <Button variant='light' className='navbar-button' onClick={() => navigate('/profile/' + props.email)}>
            <span className='d-flex justify-content-center align-items-center'><Person />{props.name}'s profile ({props.role})</span>
          </Button>
        </Col>
        <Col>
          <Button variant='light' className='navbar-button' onClick={() => props.doLogout()}>
            <span className='d-flex justify-content-center align-items-center'><BoxArrowLeft />Logout</span>
          </Button>
        </Col>
      </Row>
      <Row>
        <Outlet />
      </Row>
    </Container>
  );
}

export default App;