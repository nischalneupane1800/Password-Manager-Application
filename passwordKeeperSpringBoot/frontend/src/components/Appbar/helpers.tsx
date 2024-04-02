import React from "react";
import {AppState} from "../../app-types";
import {Button} from "@material-ui/core";
import {throwAndLogExceptions} from "../../throw-and-log-exceptions";

const handleRegisterStateChange = (
  setApplicationState: React.Dispatch<React.SetStateAction<AppState>>,
  value: boolean
) => {
  try {
    setApplicationState((prevState: AppState) => ({
      ...prevState,
      register: value
    }));
  } catch (e: any) {
    throwAndLogExceptions(e);
  }
}

const handleIndexSelectedChange = (
  setApplicationState: React.Dispatch<React.SetStateAction<AppState>>,
  value: boolean
) => {
  try {
    setApplicationState((prevState: AppState) => ({
      ...prevState,
      indexSelected: value
    }));
  } catch (e: any) {
    throwAndLogExceptions(e);
  }
}

const logoutUser = (
  setApplicationState: React.Dispatch<React.SetStateAction<AppState>>,
  history: any
): void => {
  try {
    // Remove items from localStorage
    window.localStorage.removeItem('Uuid');
    window.localStorage.removeItem('enabled');

    // Update application state to log the user out
    setApplicationState((prevState: AppState) => ({
      ...prevState,
      currentUserUuid: null,
      enabledUser: null
    }));

    // Redirect to the login page
    history.push("/login");
  } catch (e: any) {
    throwAndLogExceptions(e);
  }
};

// Handle when the user clicks on the appbar buttons
const buttonClick = function (
  switcherButton: any,
  history: any,
  applicationState: AppState,
  setApplicationState: React.Dispatch<React.SetStateAction<AppState>>
) {
  try {
    if (switcherButton === "view") return handleIndexSelectedChange(setApplicationState, true);
    if (switcherButton === "create") return handleIndexSelectedChange(setApplicationState, false);
    if (applicationState.currentUserUuid) return logoutUser(setApplicationState, history);

    // Handle the case where the user is trying to navigate from login page to registration page
    if (!applicationState.register) {
      handleRegisterStateChange(setApplicationState, true);
      return history.push("/register")
    }

    // Handle the case where the user is trying to navigate from registration page to login page
    if (applicationState.register) {
      handleRegisterStateChange(setApplicationState, false);
      return history.push("/login")
    }
  } catch (e: any) {
    throwAndLogExceptions(e);
  }
}

// Define a reusable function to create buttons
const createButton = (label: any, onClickHandler: any) => (
  <Button color="inherit" onClick={onClickHandler}>
    {label}
  </Button>
);

// Define an array of button data (label and switcherButton value)
const buttons = [
  { label: "View Passwords", switcherButton: "view" },
  { label: "Create New Password", switcherButton: "create" },
  { label: "Logout", switcherButton: "default" },
];

export {
  buttonClick,
  handleRegisterStateChange,
  handleIndexSelectedChange,
  logoutUser,
  createButton,
  buttons
}